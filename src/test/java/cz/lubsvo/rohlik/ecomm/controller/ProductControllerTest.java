package cz.lubsvo.rohlik.ecomm.controller;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.lubsvo.rohlik.ecomm.exception.ProductNotFoundException;
import cz.lubsvo.rohlik.ecomm.model.Product;
import cz.lubsvo.rohlik.ecomm.contoller.model.ProductReqeust;
import cz.lubsvo.rohlik.ecomm.model.ProductStatus;
import cz.lubsvo.rohlik.ecomm.service.ProductService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
public class ProductControllerTest {

    private static final long PRODUCT_ID = 100L;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    public void shouldReturnSuccessWhenProductCreated() throws Exception {
        var createdProduct = new Product()
                .setId(PRODUCT_ID)
                .setStatus(ProductStatus.ACTIVE)
                .setName("Water")
                .setPrice(BigDecimal.TEN)
                .setQuantity(1);
        doReturn(createdProduct)
                .when(productService).create(any(Product.class));

        var p = new ProductReqeust()
                .setName("Water")
                .setPrice(BigDecimal.TEN)
                .setQuantity(1);
        this.mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(p)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(PRODUCT_ID));
    }

    @Test
    public void shouldReturn404ErrorWhenProductNotFound() throws Exception {
        doThrow(new ProductNotFoundException("Product not found"))
                .when(productService).update(anyLong(), any(Product.class));

        var p = new ProductReqeust()
                .setName("Water")
                .setPrice(BigDecimal.TEN)
                .setQuantity(1);
        this.mockMvc.perform(put("/api/products/1").contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(p)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("404"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Not Found"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Product not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.path").value("/api/products/1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists());
    }

    // TODO more tests

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
