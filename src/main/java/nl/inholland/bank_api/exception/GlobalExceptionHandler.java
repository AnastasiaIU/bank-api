package nl.inholland.bank_api.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.java.Log;
import nl.inholland.bank_api.constant.ErrorMessages;
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

import java.util.List;
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
        log.log(Level.SEVERE, ErrorMessages.UNHANDLED_EXCEPTION, e);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, e, ErrorMessages.UNEXPECTED_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionDTO> handleIllegalArgument(IllegalArgumentException e) {
        log.severe(e.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, e, e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionDTO> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.severe(e.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, e, ErrorMessages.DATA_INTEGRITY_VIOLATION);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ExceptionDTO> handleEntityNotFound(EntityNotFoundException e) {
        log.severe(e.getMessage());
        return buildError(HttpStatus.NOT_FOUND, e, e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionDTO> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.severe(e.getMessage());
        return buildError(HttpStatus.METHOD_NOT_ALLOWED, e, ErrorMessages.HTTP_METHOD_NOT_SUPPORTED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionDTO> handleAccessDenied(AccessDeniedException e) {
        log.severe(e.getMessage());
        return buildError(HttpStatus.FORBIDDEN, e, ErrorMessages.ACCESS_DENIED);
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

        return buildError(HttpStatus.BAD_REQUEST, e, validationErrors.toArray(new String[0]));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionDTO> handleConstraintViolation(ConstraintViolationException e) {
        log.severe(e.getMessage());

        List<String> violations = e.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .distinct()
                .toList();

        return buildError(HttpStatus.BAD_REQUEST, e, violations.toArray(new String[0]));
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
    public ResponseEntity<ExceptionDTO> handleUsernameNotFoundException(UsernameNotFoundException e) {
        log.severe(e.getMessage());
        return buildError(HttpStatus.NOT_FOUND, e, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionDTO> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.severe(e.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, e, e.getMessage());
    }
}
