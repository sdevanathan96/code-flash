package com.codeflash.controller.rest;

import com.codeflash.dto.response.ErrorResponse;
import com.codeflash.exception.ResourceNotFoundException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
       .map(e -> e.getField() + ": " + e.getDefaultMessage())
       .collect(Collectors.joining(", "));
    return ResponseEntity.badRequest()
       .body(new ErrorResponse("VALIDATION_ERROR", message, LocalDateTime.now()));

  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUndefinedResource(
      ResourceNotFoundException ex){
    log.warn("Resource not found: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            new ErrorResponse(
                "NOT_FOUND", ex.getMessage(), LocalDateTime.now()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
     IllegalArgumentException ex){
    log.warn("Bad request: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            new ErrorResponse(
                "BAD_REQUEST", ex.getMessage(), LocalDateTime.now()));
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(
     NoSuchElementException ex){
    log.warn("Element(Id) not found: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            new ErrorResponse(
                "NOT_FOUND", ex.getMessage(), LocalDateTime.now()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
    log.error("Unhandled exception", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            new ErrorResponse(
                "INTERNAL_ERROR", "An unexpected error occurred", LocalDateTime.now()));
  }
}