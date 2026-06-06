package com.authentication.auth_app_backend.modules.user;

import com.authentication.auth_app_backend.modules.user.dto.UserDto;
import com.authentication.auth_app_backend.modules.user.dto.UserProfileDto;
import com.authentication.auth_app_backend.modules.user.dto.UserResponseDto;

public interface UserService {

  // Create user
  UserResponseDto createUser(UserDto user);

  // get user by email
  UserResponseDto getUserByEmail(String email);

  // update user
  UserResponseDto updateUser(UserDto user, String userId);

  // delete user
  void deleteUser(String userId);

  // get user by id
  UserResponseDto getUserById(String userId);

  // get all users
  Iterable<UserResponseDto> getAllUsers();

  // update user
  UserResponseDto updateUserProfile(UserProfileDto user, String userId);
}
