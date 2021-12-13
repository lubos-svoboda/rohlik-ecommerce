package cz.lubsvo.rohlik.ecomm.service;

import java.math.BigDecimal;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import cz.lubsvo.rohlik.ecomm.exception.ProductQuantityNotAvailableException;
import cz.lubsvo.rohlik.ecomm.model.OrderItem;
import cz.lubsvo.rohlik.ecomm.model.Product;
import cz.lubsvo.rohlik.ecomm.model.ProductStatus;

import static cz.lubsvo.rohlik.ecomm.model.OrderStatus.CREATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
public class OrderServiceITest {

    private static final int ORDERED_QUANTITY_1 = 100;
    private static final int ORDERED_QUANTITY_2 = 200;
    private static final int OVER_LIMIT_QUANTITIES = 13;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Test
    void shouldCreateNewOrder() {
        var p1 = buildRandomProduct("Product 1");
        var p2 = buildRandomProduct("Product 2");

        var orderedProducts = Set.of(
                new OrderItem(p1.getId(), ORDERED_QUANTITY_1),
                new OrderItem(p2.getId(), ORDERED_QUANTITY_2));

        var order = orderService.create(orderedProducts);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(CREATED);
        var p1Updated = productService.findById(p1.getId());
        var p2Updated = productService.findById(p2.getId());

        assertThat(p1.getQuantity() - ORDERED_QUANTITY_1).isEqualTo(p1Updated.getQuantity());
        assertThat(p2.getQuantity() - ORDERED_QUANTITY_2).isEqualTo(p2Updated.getQuantity());
    }

    @Test
    void shouldReleaseQuantitiesWhenOrderIsCanceled() {
        var p1 = buildRandomProduct("Product 1");
        var p2 = buildRandomProduct("Product 2");
        var p3 = buildRandomProduct("Product 3");
        var orderedProducts = Set.of(
                new OrderItem(p1.getId(), ORDERED_QUANTITY_1),
                new OrderItem(p2.getId(), ORDERED_QUANTITY_2));

        var order = orderService.create(orderedProducts);

        orderService.cancel(order.getId());

        var p1Updated = productService.findById(p1.getId());
        var p2Updated = productService.findById(p2.getId());
        var p3Updated = productService.findById(p3.getId());

        assertThat(p1.getQuantity()).isEqualTo(p1Updated.getQuantity());
        assertThat(p2.getQuantity()).isEqualTo(p2Updated.getQuantity());
        assertThat(p3.getQuantity()).isEqualTo(p3Updated.getQuantity());
    }

    @Test
    void shouldFailToOrderWhenMissingQuantities() {
        var p1 = buildRandomProduct("Product 3");
        var existingQuantity = p1.getQuantity();
        var orderedQuantity = existingQuantity + OVER_LIMIT_QUANTITIES;

        var exception = assertThrows(
                ProductQuantityNotAvailableException.class,
                () -> orderService.create(Set.of(new OrderItem(p1.getId(), orderedQuantity))));

        var missingQuantities = exception.getMissingQuantities();
        assertThat(missingQuantities).hasSize(1);
        var missingQuantity= missingQuantities.iterator().next();
        assertThat(missingQuantity.getProductId()).isEqualTo(p1.getId());
        assertThat(missingQuantity.getQuantity()).isEqualTo(OVER_LIMIT_QUANTITIES);
    }

    private Product buildRandomProduct(String name) {
        var p = new Product()
                .setStatus(ProductStatus.ACTIVE)
                .setName(name)
                .setQuantity(new Random().nextInt(1000, 5000))
                .setPrice(new BigDecimal(new Random().nextInt(10000)));
        return productService.create(p);
    }
}
