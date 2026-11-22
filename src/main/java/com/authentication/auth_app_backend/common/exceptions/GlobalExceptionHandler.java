package com.authentication.auth_app_backend.common.exceptions;

import com.authentication.auth_app_backend.common.dtos.ApiErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiErrorDto> handleAuthentication(
      AuthenticationException exception, HttpServletRequest request) {
    return respond(
        HttpStatus.UNAUTHORIZED,
        "AUTHENTICATION_FAILED",
        "Authentication failed!",
        request.getRequestURI());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiErrorDto> handleAccessDenied(
      AccessDeniedException exception, HttpServletRequest request) {
    return respond(
        HttpStatus.FORBIDDEN,
        "ACCESS_DENIED",
        "You don't have permission to perform this action.",
        request.getRequestURI());
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiErrorDto> handleNotFound(
      ResourceNotFoundException exception, HttpServletRequest request) {
    return respond(
        HttpStatus.NOT_FOUND,
        "RESOURCE_NOT_FOUND",
        "The requested resource was not found.",
        request.getRequestURI());
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiErrorDto> handleMissingEndpoint(
      NoResourceFoundException exception, HttpServletRequest request) {
    return respond(
        HttpStatus.NOT_FOUND,
        "RESOURCE_NOT_FOUND",
        "The requested resource was not found.",
        request.getRequestURI());
  }

  @ExceptionHandler({ResourceConflictException.class, DuplicateKeyException.class})
  public ResponseEntity<ApiErrorDto> handleConflict(
      RuntimeException exception, HttpServletRequest request) {
    String detail =
        exception instanceof ResourceConflictException
            ? safeMessage(exception, "A resource with these details already exists.")
            : "A resource with these details already exists.";
    return respond(HttpStatus.CONFLICT, "RESOURCE_CONFLICT", detail, request.getRequestURI());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiErrorDto> handleInvalidRequest(
      IllegalArgumentException exception, HttpServletRequest request) {
    return respond(
        HttpStatus.BAD_REQUEST,
        "INVALID_REQUEST",
        safeMessage(exception, "The request could not be processed."),
        request.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorDto> handleValidation(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    Map<String, String> errors =
        exception.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    FieldError::getField,
                    error -> safeMessage(error.getDefaultMessage(), "Invalid value."),
                    (first, ignored) -> first,
                    LinkedHashMap::new));
    return respond(
        HttpStatus.BAD_REQUEST,
        "VALIDATION_FAILED",
        "One or more fields are invalid.",
        request.getRequestURI(),
        errors);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiErrorDto> handleConstraintViolation(
      ConstraintViolationException exception, HttpServletRequest request) {
    Map<String, String> errors = new LinkedHashMap<>();
    exception
        .getConstraintViolations()
        .forEach(
            violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage()));
    return respond(
        HttpStatus.BAD_REQUEST,
        "VALIDATION_FAILED",
        "One or more values are invalid.",
        request.getRequestURI(),
        errors);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MissingServletRequestParameterException.class,
    MethodArgumentTypeMismatchException.class
  })
  public ResponseEntity<ApiErrorDto> handleMalformedRequest(
      Exception exception, HttpServletRequest request) {
    return respond(
        HttpStatus.BAD_REQUEST,
        "MALFORMED_REQUEST",
        "Check the request format and required values.",
        request.getRequestURI());
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiErrorDto> handleMethodNotAllowed(
      HttpRequestMethodNotSupportedException exception, HttpServletRequest request) {
    return respond(
        HttpStatus.METHOD_NOT_ALLOWED,
        "METHOD_NOT_ALLOWED",
        "This request method is not supported for this endpoint.",
        request.getRequestURI());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorDto> handleUnexpected(
      Exception exception, HttpServletRequest request) {
    ApiErrorDto error =
        ApiErrorDto.of(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_ERROR",
            "Something went wrong. Please try again.",
            request.getRequestURI());
    log.error(
        "Unhandled request error requestId={} path={}",
        error.requestId(),
        request.getRequestURI(),
        exception);
    return response(error);
  }

  private ResponseEntity<ApiErrorDto> respond(
      HttpStatus status, String code, String detail, String path) {
    return response(ApiErrorDto.of(status, code, detail, path));
  }

  private ResponseEntity<ApiErrorDto> respond(
      HttpStatus status, String code, String detail, String path, Map<String, String> errors) {
    return response(ApiErrorDto.of(status, code, detail, path, errors));
  }

  private ResponseEntity<ApiErrorDto> response(ApiErrorDto error) {
    return ResponseEntity.status(error.status())
        .contentType(MediaType.valueOf("application/problem+json"))
        .header("X-Request-Id", error.requestId())
        .body(error);
  }

  private static String safeMessage(Exception exception, String fallback) {
    return safeMessage(exception.getMessage(), fallback);
  }

  private static String safeMessage(String message, String fallback) {
    return message == null || message.isBlank() ? fallback : message;
  }
}
