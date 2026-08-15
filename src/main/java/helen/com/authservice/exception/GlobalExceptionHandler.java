package helen.com.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({EmailAlreadyExistsException.class, UsernameAlreadyExistsException.class})
    public ResponseEntity<ErrorResponse> handleAlreadyExists(RuntimeException ex) {
        ErrorResponse response =
                new ErrorResponse(
                        false,
                        ex.getMessage(),
                        List.of(),
                        LocalDateTime.now());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse response =
                new ErrorResponse(
                        false,
                        ex.getMessage(),
                        List.of(),
                        LocalDateTime.now());
        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler({UnauthorizedException.class, TokenExpiredException.class})
    public ResponseEntity<ErrorResponse> handleUnauthorized(RuntimeException ex) {
        ErrorResponse response =
                new ErrorResponse(
                        false,
                        ex.getMessage(),
                        List.of(),
                        LocalDateTime.now());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField()
                                + ": "
                                + error.getDefaultMessage())
                .toList();

        ErrorResponse response =
                new ErrorResponse(
                        false,
                        "Erro de validação",
                        errors,
                        LocalDateTime.now());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse>
    handleGenericException(
            Exception ex
    ) {

        ErrorResponse response =
                new ErrorResponse(
                        false,
                        ex.getMessage(),
                        List.of(),
                        LocalDateTime.now());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
