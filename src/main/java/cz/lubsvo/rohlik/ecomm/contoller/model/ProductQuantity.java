package cz.lubsvo.rohlik.ecomm.contoller.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ProductQuantity {

    @NotNull
    private Long productId;

    @Min(1)
    @NotNull
    private Integer quantity;

}
