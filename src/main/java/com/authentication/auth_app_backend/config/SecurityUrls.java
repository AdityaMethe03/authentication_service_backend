package com.authentication.auth_app_backend.config;

public class SecurityUrls {

  public static final String[] AUTH_PUBLIC_URLS = {
    // authentication paths endpoints
    "/api/v1/auth/register",
    "/api/v1/auth/login",
    "/api/v1/auth/refresh",
    "/api/v1/auth/logout",
    "/api/v1/auth/logout/all",

    // open api documentation paths endpoints
    "/v3/api-docs/**",
    "/api-docs/**",
    "/api-docs",
    // swagger ui endpoints
    "/swagger-ui.html",
    "/swagger-ui/**"
  };
}
