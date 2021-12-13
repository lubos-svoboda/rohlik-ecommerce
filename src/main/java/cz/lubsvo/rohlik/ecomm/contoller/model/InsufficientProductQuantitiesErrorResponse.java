package cz.lubsvo.rohlik.ecomm.contoller.model;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InsufficientProductQuantitiesErrorResponse extends ErrorResponse {

    private Set<ProductQuantity> missingQuantities;
}
