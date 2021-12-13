package cz.lubsvo.rohlik.ecomm.service;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import cz.lubsvo.rohlik.ecomm.exception.OrderActionException;
import cz.lubsvo.rohlik.ecomm.exception.ProductNotFoundException;
import cz.lubsvo.rohlik.ecomm.exception.ProductQuantityNotAvailableException;
import cz.lubsvo.rohlik.ecomm.model.Order;
import cz.lubsvo.rohlik.ecomm.model.OrderItem;
import cz.lubsvo.rohlik.ecomm.repository.OrderDao;
import cz.lubsvo.rohlik.ecomm.repository.ProductDao;
import lombok.RequiredArgsConstructor;

import static cz.lubsvo.rohlik.ecomm.model.OrderStatus.CANCELED;
import static cz.lubsvo.rohlik.ecomm.model.OrderStatus.CREATED;
import static cz.lubsvo.rohlik.ecomm.model.OrderStatus.PAID;
import static cz.lubsvo.rohlik.ecomm.model.ProductStatus.ACTIVE;

@Service
@Transactional
@RequiredArgsConstructor
@Validated
public class OrderService {

    private final OrderDao orderDao;
    private final ProductDao productDao;

    /**
     * Create a new order and reserve product quantity specified by {@code orderItems}.
     *
     * @param orderItems set of items to order including quantity
     * @return newly created order
     * @throws ProductQuantityNotAvailableException when requested product quantity exceed available quantity
     */
    public Order create(@Valid @NotNull @Size(min = 1) Set<OrderItem> orderItems) {
        checkQuantities(orderItems);
        productDao.decreaseQuantities(orderItems);

        return createNewOrder(orderItems);
    }

    /**
     * Cancel order with given {@code orderId} and release blocked product quantity.
     *
     * @param orderId identification of order
     * @throws OrderActionException when order cannot be canceled due to unexpected order status
     */
    public void cancel(@NotNull Long orderId) {
        var order = orderDao.findByIdWithLock(orderId);
        if (CREATED != order.getStatus()) {
            throw new OrderActionException("Cannot cancel order, it has been already canceled or paid");
        }
        orderDao.updateStatusById(orderId, CANCELED);
        productDao.increaseQuantities(order.getOrderedItems());
    }

    /**
     * Pay order with given {@code orderId} - change status of the order
     *
     * @param orderId identification of order
     * @throws OrderActionException when order cannot be paid due to unexpected order status
     */
    public void pay(@NotNull Long orderId) {
        var order = orderDao.findByIdWithLock(orderId);
        if (CREATED != order.getStatus()) {
            throw new OrderActionException("Cannot pay order, it has been already canceled or paid");
        }
        orderDao.updateStatusById(orderId, PAID);
    }

    private Order createNewOrder(Set<OrderItem> orderedProductQuantities) {
        var order = new Order()
                .setStatus(CREATED)
                .setOrderedItems(orderedProductQuantities);

        return orderDao.create(order);
    }

    private void checkQuantities(Set<OrderItem> orderedProductQuantities) {
        var productIds = orderedProductQuantities.stream()
                .map(OrderItem::getProductId)
                .collect(Collectors.toSet());
        var availableProducts = productDao.findByStatusAndIdsWithLock(ACTIVE, productIds);
        var missingQuantities = new LinkedHashSet<OrderItem>();
        for (var entry : orderedProductQuantities) {
            var productId = entry.getProductId();
            var requestedQuantity = entry.getQuantity();
            if (!availableProducts.containsKey(productId)) {
                throw new ProductNotFoundException(String.format("Product '%s' not found", productId));
            }
            var p = availableProducts.get(productId);
            if (p.getQuantity() < requestedQuantity) {
                missingQuantities.add(new OrderItem()
                        .setProductId(productId)
                        .setQuantity(requestedQuantity - p.getQuantity()));
            }
        }
        if (!missingQuantities.isEmpty()) {
            throw new ProductQuantityNotAvailableException("Cannot order, missing quantity found", missingQuantities);
        }
    }
}
