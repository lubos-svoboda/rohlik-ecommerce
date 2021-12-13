package cz.lubsvo.rohlik.ecomm.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(final String message) {
        super(message);
    }
}
