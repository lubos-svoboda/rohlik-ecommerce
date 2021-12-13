package cz.lubsvo.rohlik.ecomm.controller;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.lubsvo.rohlik.ecomm.exception.ProductQuantityNotAvailableException;
import cz.lubsvo.rohlik.ecomm.contoller.model.OrderRequest;
import cz.lubsvo.rohlik.ecomm.contoller.model.ProductQuantity;
import cz.lubsvo.rohlik.ecomm.model.Order;
import cz.lubsvo.rohlik.ecomm.model.OrderItem;
import cz.lubsvo.rohlik.ecomm.service.OrderService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
public class OrderControllerTest {

    private static final long ORDER_ID = 1L;
    private static final long PRODUCT_ID = 100L;
    private static final int MISSING_QUANTITY = 10;
    private static final int PRODUCT_QUANTITY = 20;
    private static final String EXC_MESSAGE = "Missing quantity";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    public void shouldReturnIdWhenOrderCreated() throws Exception {
        var orderRequest = new OrderRequest()
                .setProductQuantities(Set.of(new ProductQuantity(PRODUCT_ID, PRODUCT_QUANTITY)));

        when(orderService.create(any())).thenReturn(new Order().setId(ORDER_ID));

        this.mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(orderRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(ORDER_ID));
        ArgumentCaptor<Set<OrderItem>> orderItemsArgumentCaptor = ArgumentCaptor.forClass(Set.class);
        verify(orderService).create(orderItemsArgumentCaptor.capture());

        var orderItems = orderItemsArgumentCaptor.getValue();
        assertThat(orderItems).hasSize(1);
        assertThat(orderItems).contains(new OrderItem(PRODUCT_ID, PRODUCT_QUANTITY));
    }

    @Test
    public void shouldReturn409ErrorWhenNotEnoughQuantities() throws Exception {
        var orderRequest = new OrderRequest()
                .setProductQuantities(Set.of(new ProductQuantity(PRODUCT_ID, PRODUCT_QUANTITY)));

        Set<OrderItem> missingQuantities = Set.of(new OrderItem(PRODUCT_ID, MISSING_QUANTITY));
        doThrow(new ProductQuantityNotAvailableException(EXC_MESSAGE, missingQuantities))
                .when(orderService).create(any());

        this.mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(orderRequest)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("409"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Conflict"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(EXC_MESSAGE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.path").value("/api/orders"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.missingQuantities[0].productId").value(PRODUCT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.missingQuantities[0].quantity").value(MISSING_QUANTITY));
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
