package com.authentication.auth_app_backend.modules.user;

import com.authentication.auth_app_backend.modules.user.dto.*;
import com.authentication.auth_app_backend.modules.user.enums.UserStatusEnum;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tags(@Tag(name = "V1 User"))
@AllArgsConstructor
@CrossOrigin
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService userService;

  /***  Register apis ***/
  @PostMapping(value = "/register")
  public ResponseEntity<UserResponseDto> createUser(@RequestBody UserDto user) {
    user.setId(UUID.randomUUID().toString());
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(user));
  }

  @PostMapping(value = "/register/admin")
  public ResponseEntity<UserResponseDto> createAdminUser(@RequestBody UserAdminDto user) {
    user.setId(UUID.randomUUID().toString());
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.createAdminUser(user));
  }

  /***  Update apis ***/
  @PutMapping(value = "/update/{userId}")
  public ResponseEntity<UserResponseDto> updateUser(
      @RequestBody UserDto user, @PathVariable String userId) {
    return ResponseEntity.ok(userService.updateUser(user, userId));
  }

  @PutMapping(value = "/update/profile/{userId}")
  public ResponseEntity<UserResponseDto> updateUserProfile(
      @RequestBody UserProfileDto user, @PathVariable String userId) {
    return ResponseEntity.ok(userService.updateUserProfile(user, userId));
  }

  @PutMapping(value = "/update/password/{userId}")
  public ResponseEntity<UserResponseDto> updateUserPassword(
      @RequestBody UserPasswordDto user, @PathVariable String userId) {
    return ResponseEntity.ok(userService.updateUserPassword(user, userId));
  }

  @PutMapping(value = "/update/status/{userId}")
  public ResponseEntity<UserResponseDto> updateUserStatus(
      @RequestParam UserStatusEnum status, @PathVariable String userId) {
    return ResponseEntity.ok(userService.updateUserStatusById(status, userId));
  }

  /***  Delete apis ***/
  @DeleteMapping(value = "/delete/{userId}")
  public void deleteUser(@PathVariable String userId) {
    userService.deleteUser(userId);
  }

  /***  Get apis  ***/
  @GetMapping(value = "/lookup/search/{userId}")
  public ResponseEntity<UserResponseDto> getUserById(@PathVariable String userId) {
    return ResponseEntity.ok(userService.getUserById(userId));
  }

  @GetMapping(value = "/lookup/search/email/{email}")
  public ResponseEntity<UserResponseDto> getUserByEmail(@PathVariable String email) {
    return ResponseEntity.ok(userService.getUserByEmail(email));
  }

  @GetMapping(value = "/lookup/search/all")
  public ResponseEntity<Iterable<UserResponseDto>> getAllUsers() {
    return ResponseEntity.ok(userService.getAllUsers());
  }
}
