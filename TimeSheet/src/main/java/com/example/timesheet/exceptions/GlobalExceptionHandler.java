package com.example.timesheet.exceptions;

import com.example.timesheet.constants.HttpExceptionConstants;
import com.example.timesheet.constants.TimesheetErrorMessages;
import com.example.timesheet.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExists(AlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse("TIMESHEET_CONFLICT_ERROR", ex.getMessage(), "")
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse("TIMESHEET_NOT_FOUND_ERROR", ex.getMessage(), "")
        );
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ErrorResponse> handleInternal(InternalServerException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("TIMESHEET_INTERNAL_SERVER_ERROR", ex.getMessage(), "")
        );
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String field = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getField();
        String msg = String.format(TimesheetErrorMessages.VALIDATION_FAILED, field);

        ErrorResponse response = ErrorResponse.builder()
                .error_code(HttpExceptionConstants.VALIDATION_ERROR)
                .message(msg)
                .property(field)
                .build();

        log.warn("Validation error: {}", response);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .error_code(HttpExceptionConstants.VALIDATION_ERROR)
                .message(ex.getMessage())
                .property("")
                .build();

        log.warn("Constraint violation: {}", response);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DataIntegrityViolationException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .error_code(HttpExceptionConstants.CONFLICT_ERROR)
                .message("Data conflict: " + ex.getMostSpecificCause().getMessage())
                .property("")
                .build();

        log.warn("Data conflict: {}", response);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(SecurityException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .error_code(HttpExceptionConstants.FORBIDDEN_ERROR)
                .message(TimesheetErrorMessages.UNAUTHORIZED_ACCESS)
                .property("")
                .build();

        log.warn("Forbidden access: {}", response);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .error_code(HttpExceptionConstants.UNAUTHORIZED_ERROR)
                .message(ex.getMessage())
                .property("")
                .build();

        log.warn("Unauthorized access: {}", response);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .error_code(HttpExceptionConstants.NOT_FOUND_ERROR)
                .message(ex.getMessage())
                .property("")
                .build();

        log.warn("Resource not found: {}", response);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        ErrorResponse response = ErrorResponse.builder()
                .error_code(HttpExceptionConstants.INTERNAL_SERVER_ERROR)
                .message(TimesheetErrorMessages.INTERNAL_SERVER_ERROR)
                .property("")
                .build();

        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
