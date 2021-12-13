package cz.lubsvo.rohlik.ecomm.service;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import cz.lubsvo.rohlik.ecomm.exception.ProductQuantityNotAvailableException;
import cz.lubsvo.rohlik.ecomm.model.Order;
import cz.lubsvo.rohlik.ecomm.model.OrderItem;
import cz.lubsvo.rohlik.ecomm.model.OrderStatus;
import cz.lubsvo.rohlik.ecomm.model.Product;
import cz.lubsvo.rohlik.ecomm.repository.OrderDao;
import cz.lubsvo.rohlik.ecomm.repository.ProductDao;

import static cz.lubsvo.rohlik.ecomm.model.OrderStatus.CREATED;
import static cz.lubsvo.rohlik.ecomm.model.ProductStatus.ACTIVE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class OrderServiceMockTest {

    private static final long ORDER_ID = 1L;
    private static final long PRODUCT_ID = 10L;
    private static final int ORDERED_QUANTITY = 100;
    private static final int AVAILABLE_QUANTITY = 5;

    @Mock
    private OrderDao orderDao;

    @Mock
    private ProductDao productDao;

    @InjectMocks
    private OrderService fixture;

    @Test
    void shouldUpdateStatusWhenPaidSuccessfully() {
        var order = new Order()
                .setOrderedItems(Set.of(new OrderItem(PRODUCT_ID, ORDERED_QUANTITY)))
                .setStatus(CREATED);
        when(orderDao.findByIdWithLock(ORDER_ID)).thenReturn(order);

        fixture.pay(ORDER_ID);

        verify(orderDao).updateStatusById(ORDER_ID, OrderStatus.PAID);
    }

    @Test
    void shouldCreateNewOrder() {
        var product = buildSampleProduct(PRODUCT_ID, Integer.MAX_VALUE);
        var orderedProductQuantities = Set.of(new OrderItem(PRODUCT_ID, ORDERED_QUANTITY));
        var order = new Order()
                .setOrderedItems(orderedProductQuantities)
                .setStatus(CREATED);

        when(productDao.findByStatusAndIdsWithLock(ACTIVE, Set.of(PRODUCT_ID))).thenReturn(Map.of(PRODUCT_ID, product));
        when(orderDao.create(order)).thenReturn(order);

        fixture.create(Set.of(new OrderItem(PRODUCT_ID, ORDERED_QUANTITY)));

        verify(productDao).decreaseQuantities(orderedProductQuantities);
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderDao).create(orderCaptor.capture());
        var orderToSave = orderCaptor.getValue();
        assertThat(orderToSave.getStatus()).isEqualTo(CREATED);
        OrderItem productQuantity = orderToSave.getOrderedItems().iterator().next();
        assertThat(productQuantity.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(productQuantity.getQuantity()).isEqualTo(ORDERED_QUANTITY);
    }

    @Test
    void shouldFailToOrderWhenInsufficientQuantity() {
        Product product = buildSampleProduct(PRODUCT_ID, AVAILABLE_QUANTITY);

        when(productDao.findByStatusAndIdsWithLock(ACTIVE, Set.of(PRODUCT_ID))).thenReturn(Map.of(PRODUCT_ID, product));

        var exception = assertThrows(
                ProductQuantityNotAvailableException.class,
                () -> fixture.create(Set.of(new OrderItem(PRODUCT_ID, ORDERED_QUANTITY))));

        var missingQuantities = exception.getMissingQuantities();
        assertThat(missingQuantities.size()).isGreaterThan(0);
        var missingQuantity= missingQuantities.iterator().next();
        assertThat(missingQuantity.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(missingQuantity.getQuantity()).isEqualTo(ORDERED_QUANTITY - AVAILABLE_QUANTITY);
    }

    private Product buildSampleProduct(long productId, int availableQuantity) {
        return new Product()
                .setId(productId)
                .setQuantity(availableQuantity);
    }

    // TODO more tests
}
