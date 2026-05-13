package com.eaglebank.backend.exception;

import com.eaglebank.backend.dto.BadRequestErrorResponse;
import com.eaglebank.backend.dto.ErrorResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BadRequestErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<BadRequestErrorResponse.Detail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toDetail)
                .collect(Collectors.toList());

        String message = details.isEmpty() ? "Invalid request" : details.size() + " validation error(s)";

        BadRequestErrorResponse body = BadRequestErrorResponse.builder()
                .message(message)
                .details(details)
                .build();
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    private BadRequestErrorResponse.Detail toDetail(FieldError fe) {
        String msg = fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid";
        String type = mapErrorType(fe);
        return BadRequestErrorResponse.Detail.builder()
                .field(fe.getField())
                .message(msg)
                .type(type)
                .build();
    }

    private String mapErrorType(FieldError fe) {
        String code = fe.getCode();
        if (code == null) return "invalid";
        // Common Bean Validation/Spring codes mapping
        switch (code) {
            case "NotNull":
            case "NotBlank":
            case "NotEmpty":
                return "required";
            case "Pattern":
                return "pattern";
            case "TypeMismatch":
                return "typeMismatch";
            case "Size":
                return "size";
            case "Email":
                return "format";
            default:
                return "invalid";
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        // Client supplied invalid input (e.g., unsupported enum value)
        String message = ex.getMessage() != null ? ex.getMessage() : "Invalid request";
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        // Triggered by JSON parse/bind errors (e.g., invalid enum). Map to 400 with a helpful message.
        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof IllegalArgumentException) {
            String message = cause.getMessage() != null ? cause.getMessage() : "Invalid request payload";
            return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new ErrorResponse("Invalid request payload"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
        // As per OpenAPI: 422 Unprocessable Entity with ErrorResponse schema
        String message = ex.getMessage() != null ? ex.getMessage() : "Insufficient funds to process transaction";
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(UserDeletionConflictException.class)
    public ResponseEntity<ErrorResponse> handleUserDeletionConflict(UserDeletionConflictException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "The user is not allowed to access the transaction";
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        return new ResponseEntity<>(new ErrorResponse("An unexpected error occurred"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
