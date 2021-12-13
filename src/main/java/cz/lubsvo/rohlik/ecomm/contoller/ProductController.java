package cz.lubsvo.rohlik.ecomm.contoller;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cz.lubsvo.rohlik.ecomm.contoller.model.ErrorResponse;
import cz.lubsvo.rohlik.ecomm.contoller.model.IdWrapperResponse;
import cz.lubsvo.rohlik.ecomm.contoller.model.ProductReqeust;
import cz.lubsvo.rohlik.ecomm.model.Product;
import cz.lubsvo.rohlik.ecomm.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping(value = "/api/products", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Create a new product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created", content = {@Content}),
            @ApiResponse(responseCode = "400", description = "Bad request or validation failed",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))}),
    })
    @PostMapping
    ResponseEntity<IdWrapperResponse> create(
            @NotNull
            @Valid
            @RequestBody
            @Parameter(description = "Product to be created", required = true) ProductReqeust productRequest) {
        var savedProduct = productService.create(buildProductFromRequest(productRequest));
        return ResponseEntity
                .status(CREATED)
                .body(new IdWrapperResponse(savedProduct.getId()));
    }

    @Operation(summary = "Update an existing product by 'productId'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted", content = {@Content}),
            @ApiResponse(responseCode = "400", description = "Bad request or validation failed",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))}),
    })
    @PutMapping("/{productId}")
    ResponseEntity<Void> update(
            @PathVariable
            @Parameter(description = "Identification of product", required = true) Long productId,
            @NotNull
            @Valid
            @RequestBody
            @Parameter(description = "Identification of product", required = true) ProductReqeust productRequest) {
        productService.update(productId, buildProductFromRequest(productRequest));
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @Operation(summary = "Delete a product by 'productId' (marks product as inactive).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted", content = {@Content}),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))}),
    })
    @DeleteMapping("/{productId}")
    ResponseEntity<Void> delete(
            @PathVariable
            @Parameter(description = "Identification of product", required = true) Long productId) {
        productService.deleteSoftly(productId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    private Product buildProductFromRequest(ProductReqeust productRequest) {
        return new Product()
                .setName(productRequest.getName())
                .setPrice(productRequest.getPrice())
                .setQuantity(productRequest.getQuantity());
    }
}
