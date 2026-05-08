package com.authentication.auth_app_backend.services;

import com.authentication.auth_app_backend.dtos.UserDto;

public interface AuthService {
  UserDto registerUser(UserDto userDto);

  // login user
}
