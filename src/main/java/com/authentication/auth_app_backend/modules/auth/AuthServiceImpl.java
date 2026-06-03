package com.authentication.auth_app_backend.modules.auth;

import com.authentication.auth_app_backend.modules.user.UserService;
import com.authentication.auth_app_backend.modules.user.dto.UserDto;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;

  @Override
  public UserDto registerUser(UserDto userDto) {

    // logic
    // verifying email or password etc.
    // default role
    userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
    return userService.createUser(userDto);
  }
}
