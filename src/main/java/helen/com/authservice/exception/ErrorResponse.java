package helen.com.authservice.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error payload returned by GlobalExceptionHandler.
 */
public record ErrorResponse(
        boolean success,
        String message,
        List<String> errors,
        LocalDateTime timestamp
) {
}
