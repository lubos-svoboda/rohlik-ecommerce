package cz.lubsvo.rohlik.ecomm.repository;

import org.springframework.stereotype.Service;

import cz.lubsvo.rohlik.ecomm.exception.OrderNotFoundException;
import cz.lubsvo.rohlik.ecomm.model.Order;
import cz.lubsvo.rohlik.ecomm.model.OrderStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderDao {

    private final OrderMapper orderMapper;

    public Order create(Order order) {
        orderMapper.create(order);
        orderMapper.createOrderItems(order.getId(), order.getOrderedItems());
        return order;
    }

    public void updateStatusById(Long orderId, OrderStatus status) {
        var rowCount = orderMapper.updateStatusById(orderId, status);
        if (rowCount == 0) {
            throw new OrderNotFoundException(String.format("Order '%s' not found", orderId));
        }
    }

    public Order findByIdWithLock(Long orderId) {
        return orderMapper.findByIdWithLock(orderId)
                .orElseThrow(() -> new OrderNotFoundException(String.format("Order '%s' not found", orderId)));
    }
}
