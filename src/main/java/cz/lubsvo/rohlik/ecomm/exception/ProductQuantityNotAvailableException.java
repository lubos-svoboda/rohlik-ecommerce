package cz.lubsvo.rohlik.ecomm.exception;

import java.util.Set;

import cz.lubsvo.rohlik.ecomm.model.OrderItem;

public class ProductQuantityNotAvailableException extends RuntimeException {

    private final Set<OrderItem> missingQuantities;

    public ProductQuantityNotAvailableException(String message, Set<OrderItem> missingQuantities) {
        super(message);
        this.missingQuantities = missingQuantities;
    }

    public Set<OrderItem> getMissingQuantities() {
        return missingQuantities;
    }
}
