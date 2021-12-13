package cz.lubsvo.rohlik.ecomm.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(final String message) {
        super(message);
    }
}
