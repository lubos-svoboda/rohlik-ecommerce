package cz.lubsvo.rohlik.ecomm.service;

import java.math.BigDecimal;

import javax.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import cz.lubsvo.rohlik.ecomm.exception.ProductNotFoundException;
import cz.lubsvo.rohlik.ecomm.model.Product;
import cz.lubsvo.rohlik.ecomm.model.ProductStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
public class ProductServiceITest {

    private static final long NON_EXISTING_PRODUCT_ID = -2L;
    @Autowired
    private ProductService productService;

    @Test
    void shouldCreateNewProduct() {
        var p = buildSampleProduct();

        var productId = productService.create(p).getId();

        assertThat(productId).isNotNull();
        assertThat(productService.findById(productId)).isNotNull();
    }

    @Test
    void shouldFailWhenProductNameTooShort() {
        var p = buildSampleProduct();
        var productId = productService.create(p).getId();
        p.setName("Ro");

        var exception = assertThrows(ConstraintViolationException.class, () -> productService.update(productId, p));
        assertThat(exception.getMessage()).contains("size must be between 3 and 128");
    }

    @Test
    void shouldUpdateProduct() {
        var p = buildSampleProduct();
        var productId = productService.create(p).getId();
        p.setPrice(new BigDecimal("109.80"));

        productService.update(productId, p);

        var updatedProduct = productService.findById(productId);
        assertThat(updatedProduct).isNotNull();
        assertThat(updatedProduct.getPrice()).isEqualTo(p.getPrice());
    }

    @Test
    void shouldThrowExceptionWhenProductNotUpdated() {
        var product = buildSampleProduct();

        var exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.update(NON_EXISTING_PRODUCT_ID, product));

        assertThat(exception.getMessage()).isEqualTo("Product '" + NON_EXISTING_PRODUCT_ID + "' not found");
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        var exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.findById(NON_EXISTING_PRODUCT_ID));

        assertThat(exception.getMessage()).isEqualTo("Product '" + NON_EXISTING_PRODUCT_ID + "' not found");
    }

    private Product buildSampleProduct() {
        return new Product()
                .setName("Ron")
                .setPrice(new BigDecimal("99.70"))
                .setQuantity(2)
                .setStatus(ProductStatus.ACTIVE);
    }
}
