package cz.lubsvo.rohlik.ecomm.service;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import cz.lubsvo.rohlik.ecomm.model.Product;
import cz.lubsvo.rohlik.ecomm.model.ProductStatus;
import cz.lubsvo.rohlik.ecomm.repository.ProductDao;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
@Validated
public class ProductService {

    private final ProductDao productDao;

    /**
     * Find Product by product Id
     * @param id identification of product
     * @return product
     */
    @Transactional(readOnly = true)
    public Product findById(@NotNull Long id) {
        return productDao.findById(id);
    }

    /**
     * Create a new Product.
     * @param product new product to create
     * @return newly created product
     */
    public Product create(@Valid Product product) {
        return productDao.create(product);
    }

    /**
     * Update existing product
     * @param productId identification or product
     * @param product product to update
     */
    public void update(Long productId, @Valid Product product) {
        productDao.update(productId, product);
    }

    /**
     * Update status of the product so that product is not available to buy but existing orders
     * can be processed.
     * @param productId identification or product
     */
    public void deleteSoftly(@NotNull Long productId) {
        productDao.updateStatusById(productId, ProductStatus.INACTIVE);
    }
}
