package cz.lubsvo.rohlik.ecomm.repository;

import java.util.Optional;
import java.util.Set;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import cz.lubsvo.rohlik.ecomm.model.Order;
import cz.lubsvo.rohlik.ecomm.model.OrderItem;
import cz.lubsvo.rohlik.ecomm.model.OrderStatus;

@Mapper
public interface OrderMapper {

    @Select("SELECT o.* FROM `order` o WHERE o.id=#{orderId} FOR UPDATE")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "status", column = "status"),
            @Result(property = "orderedItems", javaType = Set.class, column = "id",
                    many = @Many(select = "findOrderItemsByOrderId"))
    })
    Optional<Order> findByIdWithLock(Long orderId);

    @Select("SELECT oi.* FROM order_item oi WHERE oi.order_id=#{orderId}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "orderId", column = "order_id"),
            @Result(property = "productId", column = "product_id"),
            @Result(property = "quantity", column = "quantity")
    })
    Set<OrderItem> findOrderItemsByOrderId(Long orderId);

    @Insert("INSERT INTO `order` SET status=#{status}")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void create(Order order);

    @Update("UPDATE `order` SET status=#{status} WHERE id=#{orderId}")
    int updateStatusById(Long orderId, OrderStatus status);

    @Insert("""
            <script>
            INSERT INTO order_item (order_id, product_id, quantity) 
            VALUES
            <foreach item="orderItem" collection="orderItems" separator=",">
                 (#{id}, #{orderItem.productId}, #{orderItem.quantity})
            </foreach>
            </script>
            """)
    @Options(useGeneratedKeys = true, keyProperty = "orderItems.id")
    void createOrderItems(Long id, Set<OrderItem> orderItems);
}
