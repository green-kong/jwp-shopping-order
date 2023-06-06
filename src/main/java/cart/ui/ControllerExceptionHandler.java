package cart.ui;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import cart.dto.ErrorResponse;
import cart.exception.AuthenticationException;
import cart.exception.CartItemException;
import cart.exception.DuplicatedProductCartItemException;
import cart.exception.InvalidGradeException;
import cart.exception.InvalidOrderException;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Void> handlerAuthenticationException(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(CartItemException.IllegalMember.class)
    public ResponseEntity<ErrorResponse> handleException(CartItemException.IllegalMember e) {
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(CartItemException.UnExistedCartItem.class)
    public ResponseEntity<ErrorResponse> handleCartItemException(CartItemException.UnExistedCartItem exception) {
        return badRequestHandling(exception);
    }

    @ExceptionHandler(InvalidGradeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidGradeException(InvalidGradeException exception) {
        return badRequestHandling(exception);
    }

    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderException(InvalidOrderException exception) {
        return badRequestHandling(exception);
    }

    @ExceptionHandler(DuplicatedProductCartItemException.class)
    public ResponseEntity<ErrorResponse> DuplicatedProductCartItemException(
            DuplicatedProductCartItemException exception
    ) {
        return badRequestHandling(exception);
    }

    private ResponseEntity<ErrorResponse> badRequestHandling(RuntimeException exception) {
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }
}
