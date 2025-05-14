package nl.inholland.bank_api.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.java.Log;
import nl.inholland.bank_api.model.dto.ExceptionDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@RestControllerAdvice
@Log
public class GlobalExceptionHandler {
    private ResponseEntity<ExceptionDTO> buildError(HttpStatus status, Exception e, String... messages) {
        return ResponseEntity.status(status)
                .body(new ExceptionDTO(
                        status.value(),
                        e.getClass().getSimpleName(),
                        List.of(messages)
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDTO> handleGeneric(Exception e) {
        log.log(Level.SEVERE, "Unhandled exception", e);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, e, "Unexpected error occurred");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionDTO> handleIllegalArgument(IllegalArgumentException e) {
        log.severe(e.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, e, e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionDTO> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.severe(e.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, e, "Data Integrity Violation");
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ExceptionDTO> handleEntityNotFound(EntityNotFoundException e) {
        log.severe(e.getMessage());
        return buildError(HttpStatus.NOT_FOUND, e, e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionDTO> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.severe(e.getMessage());
        return buildError(HttpStatus.METHOD_NOT_ALLOWED, e, "HTTP method not supported");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionDTO> handleAccessDenied(AccessDeniedException e) {
        log.severe(e.getMessage());
        return buildError(HttpStatus.FORBIDDEN, e, "Access denied");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionDTO> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.severe(e.getMessage());

        List<String> validationErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct()
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionDTO(
                        HttpStatus.BAD_REQUEST.value(),
                        e.getClass().getSimpleName(),
                        validationErrors
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionDTO> handleConstraintViolation(ConstraintViolationException e) {
        log.severe(e.getMessage());

        List<String> violations = e.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .distinct()
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionDTO(
                        HttpStatus.BAD_REQUEST.value(),
                        e.getClass().getSimpleName(),
                        violations
                ));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionDTO> handleBadCredentials(BadCredentialsException e) {
        log.severe(e.getMessage());
        return buildError(HttpStatus.UNAUTHORIZED, e, e.getMessage());
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ExceptionDTO> handleInternalAuthServiceException(InternalAuthenticationServiceException e) {
        log.severe(e.getMessage());
        return buildError(HttpStatus.UNAUTHORIZED, e, e.getMessage());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleUsernameNotFoundException(UsernameNotFoundException e) {
        Map<String, String> body = new HashMap<>();
        body.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        String message = "Malformed JSON request: " + extractCause(e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    private String extractCause(Throwable e) {
        if (e.getCause() != null) {
            return e.getCause().getMessage();
        }
        return e.getMessage();
    }
}
