package com.authentication.auth_app_backend.common.dtos;

import org.springframework.http.HttpStatus;

public record ErrorResponse(String message, HttpStatus status, int statusCode) {}
