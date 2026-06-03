package com.authentication.auth_app_backend.modules.auth.dto;

import com.authentication.auth_app_backend.modules.user.dto.UserDto;

public record TokenResponse(
    String accessToken, String refreshToken, long expiresIn, String tokenType, UserDto user) {
  public static TokenResponse of(
      String accessToken, String refreshToken, long expiresIn, UserDto user) {
    return new TokenResponse(accessToken, refreshToken, expiresIn, "Bearer", user);
  }
}
