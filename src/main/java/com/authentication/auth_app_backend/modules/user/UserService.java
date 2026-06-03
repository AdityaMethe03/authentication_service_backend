package com.authentication.auth_app_backend.modules.user;

import com.authentication.auth_app_backend.modules.user.dto.UserDto;

public interface UserService {

  // Create user
  UserDto createUser(UserDto user);

  // get user by email
  UserDto getUserByEmail(String email);

  // update user
  UserDto updateUser(UserDto user, String userId);

  // delete user
  void deleteUser(String userId);

  // get user by id
  UserDto getUserById(String userId);

  // get all users
  Iterable<UserDto> getAllUsers();
}
