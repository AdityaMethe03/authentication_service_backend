package com.authentication.auth_app_backend.modules.auth;

import com.authentication.auth_app_backend.modules.user.dto.UserDto;
import com.authentication.auth_app_backend.modules.user.dto.UserResponseDto;

public interface AuthService {
  UserResponseDto registerUser(UserDto userDto);

  void revokeAllUserSessions(String userId);
}
