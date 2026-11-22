package com.authentication.auth_app_backend.common.exceptions;

public class ResourceConflictException extends RuntimeException {
  public ResourceConflictException(String message) {
    super(message);
  }
}
