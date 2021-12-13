package cz.lubsvo.rohlik.ecomm.contoller;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import javax.validation.ValidationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import cz.lubsvo.rohlik.ecomm.contoller.model.ErrorResponse;
import cz.lubsvo.rohlik.ecomm.contoller.model.InsufficientProductQuantitiesErrorResponse;
import cz.lubsvo.rohlik.ecomm.contoller.model.ProductQuantity;
import cz.lubsvo.rohlik.ecomm.exception.OrderActionException;
import cz.lubsvo.rohlik.ecomm.exception.ProductNotFoundException;
import cz.lubsvo.rohlik.ecomm.exception.ProductQuantityNotAvailableException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class GlobalRestExceptionHandler extends ResponseEntityExceptionHandler {

    static final String URI_PREFIX = "uri=";

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(
            ProductNotFoundException ex,
            WebRequest request) {
        return buildResponse(
                NOT_FOUND,
                ex.getMessage(),
                request);
    }

    @ExceptionHandler(ProductQuantityNotAvailableException.class)
    public ResponseEntity<InsufficientProductQuantitiesErrorResponse> handleProductQuantityNotAvailableException(
            ProductQuantityNotAvailableException ex,
            WebRequest request) {

        var response = buildResponseInternal(
                new InsufficientProductQuantitiesErrorResponse(),
                CONFLICT,
                ex.getMessage(),
                request);
        var missingQuantities = ex.getMissingQuantities().stream()
                .map(orderItem -> new ProductQuantity(orderItem.getProductId(), orderItem.getQuantity()))
                .collect(Collectors.toSet());
        response.setMissingQuantities(missingQuantities);
        return new ResponseEntity<>(response, CONFLICT);
    }

    @ExceptionHandler(OrderActionException.class)
    public ResponseEntity<ErrorResponse> handleOrderActionException(OrderActionException ex, WebRequest request) {
        return buildResponse(
                CONFLICT,
                ex.getMessage(),
                request);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, WebRequest request) {
        return buildResponse(
                BAD_REQUEST,
                ex.getMessage(),
                request);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> handleDefaultFallback(Throwable ex, WebRequest request) {
        return buildResponse(
                INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                request);
    }

    @Override
    protected ResponseEntity handleExceptionInternal(
            Exception ex,
            @Nullable Object body,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR == status) {
            request.setAttribute("javax.servlet.error.exception", ex, 0);
        }

        return buildResponse(
                status,
                ex.getMessage(),
                request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus httpStatus,
            String message,
            WebRequest request) {
        var response = buildResponseInternal(
                new ErrorResponse(),
                httpStatus,
                message,
                request);
        return new ResponseEntity<>(response, httpStatus);
    }

    private <T extends ErrorResponse> T buildResponseInternal(
            T response,
            HttpStatus httpStatus,
            String message,
            WebRequest request) {
        response
                .setTimestamp(LocalDateTime.now())
                .setStatus(httpStatus.value())
                .setError(httpStatus.getReasonPhrase());
        if (message != null) {
            response.setMessage(message);
        }
        var path = getRequestPathWithoutUri(request);
        if (message != null) {
            response.setPath(path);
        }
        return response;
    }

    private String getRequestPathWithoutUri(final WebRequest request) {
        if (request == null) {
            return null;
        }
        var description = request.getDescription(false);

        return (description.startsWith(URI_PREFIX)) ? description.substring(URI_PREFIX.length()) : description;
    }
}
