package com.authentication.auth_app_backend.modules.auth;

import com.authentication.auth_app_backend.modules.user.UserService;
import com.authentication.auth_app_backend.modules.user.dto.UserDto;
import com.authentication.auth_app_backend.modules.user.dto.UserResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;

  @Override
  public UserResponseDto registerUser(UserDto userDto) {
    return userService.createUser(userDto);
  }
}
