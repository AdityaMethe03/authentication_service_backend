package com.authentication.auth_app_backend.config;

public class AppConstants {

  public static final String[] AUTH_PUBLIC_URLS = {
    "/api/v1/auth/register",
    "/api/v1/auth/login",
    "/api/v1/auth/refresh",
    "/api/v1/auth/logout",
    // OpenAPI documentation paths
    "/v3/api-docs/**",
    "/api-docs/**",
    "/api-docs",
    // Swagger UI paths
    "/swagger-ui.html",
    "/swagger-ui/**"
  };

  public static final String SUDO_ADMIN_ROLE = "SUDO_ADMIN";
  public static final String ADMIN_ROLE = "ADMIN";
  public static final String GUEST_ROLE = "GUEST";
}
