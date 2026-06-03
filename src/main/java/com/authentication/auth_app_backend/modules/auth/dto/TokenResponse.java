package com.authentication.auth_app_backend.modules.auth.dto;

import com.authentication.auth_app_backend.modules.user.dto.UserResponseDto;

public record TokenResponse(
    String accessToken, String refreshToken, long expiresIn, String tokenType, UserResponseDto user) {
  public static TokenResponse of(
      String accessToken, String refreshToken, long expiresIn, UserResponseDto user) {
    return new TokenResponse(accessToken, refreshToken, expiresIn, "Bearer", user);
  }
}
