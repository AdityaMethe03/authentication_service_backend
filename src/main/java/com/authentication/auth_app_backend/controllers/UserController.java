package com.authentication.auth_app_backend.controllers;

import com.authentication.auth_app_backend.dtos.UserDto;
import com.authentication.auth_app_backend.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tags(@Tag(name = "V1 User"))
@RequestMapping("api/v1/users")
@AllArgsConstructor
@CrossOrigin
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<UserDto> createUser(@RequestBody UserDto user) {
    user.setId(UUID.randomUUID().toString());
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(user));
  }
}
