package com.authentication.auth_app_backend.services;

import com.authentication.auth_app_backend.dtos.UserDto;

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
