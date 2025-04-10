package com.example.timesheet.exceptions;

import com.example.timesheet.constants.errorCode;
import com.example.timesheet.constants.errorMessage;
import com.example.timesheet.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Removed: @ExceptionHandler(AlreadyExistsException.class) handler

    @ExceptionHandler(TimeSheetException.class)
    public ResponseEntity<ErrorResponse> handleTimeSheetException(TimeSheetException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .error_code(ex.getErrorCode())
                .message(ex.getMessage())
                .property("")
                .build();

        log.warn("TimeSheetException caught: {}", response);
        return ResponseEntity.status(resolveHttpStatus(ex.getErrorCode())).body(response);
    }

    private HttpStatus resolveHttpStatus(String errorCode) {
        return switch (errorCode) {
            case com.example.timesheet.constants.errorCode.NOT_FOUND_ERROR -> HttpStatus.NOT_FOUND;
            case com.example.timesheet.constants.errorCode.CONFLICT_ERROR -> HttpStatus.CONFLICT;
            case com.example.timesheet.constants.errorCode.FORBIDDEN_ERROR -> HttpStatus.FORBIDDEN;
            case com.example.timesheet.constants.errorCode.UNAUTHORIZED_ERROR -> HttpStatus.UNAUTHORIZED;
            case com.example.timesheet.constants.errorCode.VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
            case com.example.timesheet.constants.errorCode.INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_REQUEST;
        };
    }





    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErrorResponse>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<ErrorResponse> errorResponses = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> ErrorResponse.builder()
                        .error_code(errorCode.VALIDATION_ERROR)
                        .message(error.getDefaultMessage())
                        .property(error.getField())
                        .build())
                .toList();

        log.warn("Validation errors: {}", errorResponses);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponses);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .error_code(errorCode.VALIDATION_ERROR)
                .message(ex.getMessage())
                .property("")
                .build();

        log.warn("Constraint violation: {}", response);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DataIntegrityViolationException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .error_code(errorCode.CONFLICT_ERROR)
                .message("Data conflict: " + ex.getMostSpecificCause().getMessage())
                .property("")
                .build();

        log.warn("Data conflict: {}", response);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(SecurityException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .error_code(errorCode.FORBIDDEN_ERROR)
                .message(errorMessage.UNAUTHORIZED_ACCESS)
                .property("")
                .build();

        log.warn("Forbidden access: {}", response);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }





    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        ErrorResponse response = ErrorResponse.builder()
                .error_code(errorCode.INTERNAL_SERVER_ERROR)
                .message(errorCode.INTERNAL_SERVER_ERROR)
                .property("")
                .build();

        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
