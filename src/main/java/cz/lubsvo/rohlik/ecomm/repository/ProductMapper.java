package cz.lubsvo.rohlik.ecomm.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import cz.lubsvo.rohlik.ecomm.model.OrderItem;
import cz.lubsvo.rohlik.ecomm.model.Product;
import cz.lubsvo.rohlik.ecomm.model.ProductStatus;

@Mapper
public interface ProductMapper {

    @Select("SELECT * FROM product")
    List<Product> findAll();

    @Select("SELECT * FROM product WHERE id=#{productId}")
    Optional<Product> findById(Long productId);

    @Update("UPDATE product SET status=#{status} WHERE id=#{productId}")
    int updateStatusById(Long productId, ProductStatus status);

    @Update("UPDATE product SET name=#{product.name}, price=#{product.price}, quantity=#{product.quantity} WHERE " +
            "id=#{productId}")
    int update(Long productId, Product product);

    @Insert("INSERT INTO product SET id=#{id}, name=#{name}, price=#{price}, quantity=#{quantity}")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Long create(Product product);

     @Update("""
             <script>
             UPDATE product
             SET quantity = CASE
                <foreach item='oi' collection='orderItems'>
                                WHEN id=#{oi.productId} THEN quantity + #{oi.quantity}
                </foreach>
                            END
             WHERE id IN 
                <foreach item='oi' collection='orderItems' separator=',' open='(' close=')'>
                         #{oi.productId}
                </foreach>
             </script>
             """)
    void increaseQuantities(Set<OrderItem> orderItems);

    @Update("""
             <script>
             UPDATE product
             SET quantity = CASE
                <foreach item='oi' collection='orderItems'>
                                WHEN id=#{oi.productId} THEN quantity - #{oi.quantity}
                </foreach>
                            END
             WHERE id IN
                <foreach item='oi' collection='orderItems' separator=',' open='(' close=')'>
                         #{oi.productId}
                </foreach>
             </script>
             """)
    void decreaseQuantities(Set<OrderItem> orderItems);

    @Select("""
            <script>
            SELECT * FROM product 
            WHERE id IN 
                <foreach item='productId' collection='productIds' separator=',' open='(' close=')'>
                    #{productId}
                </foreach>
            AND status = #{status}    
            ORDER BY ID FOR UPDATE
            </script>
            """)
    List<Product> findByStatusAndIdsWithLock(ProductStatus status, Set<Long> productIds);
}
