package com.authentication.auth_app_backend.common.dtos;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

/** A consistent, safe error response following the RFC 9457 problem details shape. */
public record ApiErrorDto(
    String type,
    String title,
    int status,
    String code,
    String detail,
    String instance,
    String requestId,
    OffsetDateTime timestamp,
    Map<String, String> errors) {

  public static ApiErrorDto of(HttpStatus status, String code, String detail, String instance) {
    return of(status, code, detail, instance, Map.of());
  }

  public static ApiErrorDto of(
      HttpStatus status, String code, String detail, String instance, Map<String, String> errors) {
    return new ApiErrorDto(
        "about:blank",
        status.getReasonPhrase(),
        status.value(),
        code,
        detail,
        instance,
        UUID.randomUUID().toString(),
        OffsetDateTime.now(ZoneOffset.UTC),
        errors == null ? Map.of() : Map.copyOf(errors));
  }
}
