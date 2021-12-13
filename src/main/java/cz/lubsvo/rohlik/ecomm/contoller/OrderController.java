package cz.lubsvo.rohlik.ecomm.contoller;

import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cz.lubsvo.rohlik.ecomm.contoller.model.ErrorResponse;
import cz.lubsvo.rohlik.ecomm.contoller.model.IdWrapperResponse;
import cz.lubsvo.rohlik.ecomm.contoller.model.InsufficientProductQuantitiesErrorResponse;
import cz.lubsvo.rohlik.ecomm.contoller.model.OrderRequest;
import cz.lubsvo.rohlik.ecomm.model.OrderItem;
import cz.lubsvo.rohlik.ecomm.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping(value = "/api/orders", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create a new order and decrease quantity of each ordered product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created", content = {@Content}),
            @ApiResponse(responseCode = "400", description = "Bad request or validation failed",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "Order not created because of insufficient product " +
                    "quantities",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = InsufficientProductQuantitiesErrorResponse.class))})
    })
    @PostMapping()
    ResponseEntity<IdWrapperResponse> create(@RequestBody OrderRequest orderRequest) {
        var orderItems = orderRequest.getProductQuantities().stream()
                .map(pq -> new OrderItem(pq.getProductId(), pq.getQuantity()))
                .collect(Collectors.toSet());
        var order = orderService.create(orderItems);
        return ResponseEntity
                .status(CREATED)
                .body(new IdWrapperResponse(order.getId()));

    }

    @Operation(summary = "Cancel order identified by 'orderId' and release reserved product quantities. Order is " +
            "expected to be in 'CREATED' state.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order paid", content = {@Content}),
            @ApiResponse(responseCode = "400", description = "Bad request or validation failed",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "Order is in unexpected state and cannot be canceled.",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))})
    })
    @DeleteMapping("/{orderId}/cancel")
    ResponseEntity<Void> cancel(@PathVariable final Long orderId) {
        orderService.cancel(orderId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @Operation(summary = "Mark order identified by 'orderId' as paid. Order is expected to be in 'CREATED' state.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order paid", content = {@Content}),
            @ApiResponse(responseCode = "400", description = "Bad request or validation failed",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "Order is in unexpected state and cannot be paid.",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))})
    })
    @PostMapping("/{orderId}/pay")
    ResponseEntity<Void> payOrder(@PathVariable final Long orderId) {
        orderService.pay(orderId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }
}
