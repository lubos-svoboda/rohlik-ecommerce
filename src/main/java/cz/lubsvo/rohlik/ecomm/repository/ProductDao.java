package cz.lubsvo.rohlik.ecomm.repository;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import cz.lubsvo.rohlik.ecomm.exception.ProductNotFoundException;
import cz.lubsvo.rohlik.ecomm.model.OrderItem;
import cz.lubsvo.rohlik.ecomm.model.Product;
import cz.lubsvo.rohlik.ecomm.model.ProductStatus;
import lombok.RequiredArgsConstructor;

import static java.lang.String.*;

@Service
@RequiredArgsConstructor
public class ProductDao {

    private final ProductMapper productMapper;

    public Product findById(Long productId) {
        return productMapper.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(format("Product '%s' not found", productId)));
    }

    public void updateStatusById(Long productId, ProductStatus status) {
        var rowCount = productMapper.updateStatusById(productId, status);
        if (rowCount == 0) {
            throw new ProductNotFoundException(format("Product '%s' not found", productId));
        }
    }

    public void update(Long productId, Product product) {
        var rowCount = productMapper.update(productId, product);
        if (rowCount == 0) {
            throw new ProductNotFoundException(format("Product '%s' not found", productId));
        }
    }

    public Product create(Product product) {
        productMapper.create(product);
        return findById(product.getId());
    }

    public void increaseQuantities(Set<OrderItem> orderItems) {
        productMapper.increaseQuantities(orderItems);
    }

    public void decreaseQuantities(Set<OrderItem> orderItems) {
        productMapper.decreaseQuantities(orderItems);
    }

    public Map<Long, Product> findByStatusAndIdsWithLock(ProductStatus status, Set<Long> productIds) {
        return productMapper.findByStatusAndIdsWithLock(status, productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
    }
}
