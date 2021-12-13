package cz.lubsvo.rohlik.ecomm.contoller.model;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class OrderRequest {

    @NotNull
    @Size(min = 1)
    private Set<ProductQuantity> productQuantities = new LinkedHashSet<>();
}
