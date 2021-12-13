package cz.lubsvo.rohlik.ecomm.model;

import java.math.BigDecimal;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class Product {

    private Long id;

    private ProductStatus status;

    @NotBlank
    @Size(min = 3, max = 128)
    private String name;

    @Min(0)
    @NotNull
    private Integer quantity;

    @Min(0)
    @Max(9999999)
    @NotNull
    private BigDecimal price;
}
