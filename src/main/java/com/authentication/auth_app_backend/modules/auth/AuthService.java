package com.authentication.auth_app_backend.modules.auth;

import com.authentication.auth_app_backend.modules.user.dto.UserDto;

public interface AuthService {
  UserDto registerUser(UserDto userDto);

  // login user
}
