package gift.global;

import gift.global.error.ErrorResponse;
import gift.global.error.ObjectErrorResponse;
import gift.global.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundEntityException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundProductException(NotFoundEntityException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(CustomDatabaseException.class)
    public ResponseEntity<Map<String, String>> handleCustomDatabaseException(CustomDatabaseException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse>  handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrorResponses = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField
                        ,FieldError::getDefaultMessage
                        ,(existing, replacement) -> existing));

        List<ObjectErrorResponse> globalErrorResponses = ex.getBindingResult().getGlobalErrors()
                .stream()
                .map(globalError -> new ObjectErrorResponse(globalError.getDefaultMessage())).toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(fieldErrorResponses, globalErrorResponses));
    }

    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateEntityException(DuplicateEntityException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(AuthorizationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(BadRequestEntityException.class)
    public ResponseEntity<Map<String, String>> handleBadRequestEntityException(BadRequestEntityException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }
}
