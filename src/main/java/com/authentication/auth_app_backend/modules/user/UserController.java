package com.authentication.auth_app_backend.modules.user;

import com.authentication.auth_app_backend.modules.user.dto.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
  @PreAuthorize("hasAuthority('USER:WRITE')")
  public ResponseEntity<UserResponseDto> createUser(@RequestBody UserDto user) {
    user.setId(UUID.randomUUID().toString());
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(user));
  }

  @PostMapping(value = "/register/admin")
  @PreAuthorize("hasAuthority('USER:WRITE')")
  public ResponseEntity<UserResponseDto> createAdminUser(@RequestBody UserAdminDto user) {
    user.setId(UUID.randomUUID().toString());
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.createAdminUser(user));
  }

  /***  Update apis ***/
  @PutMapping(value = "/update/{userId}")
  @PreAuthorize("hasAuthority('USER:UPDATE')")
  public ResponseEntity<UserResponseDto> updateUser(
      @RequestBody UserDto user, @PathVariable String userId) {
    return ResponseEntity.ok(userService.updateUser(user, userId));
  }

  @PutMapping(value = "/update/all/{userId}")
  @PreAuthorize("hasAuthority('USER:UPDATE') and hasAuthority('ROLE:UPDATE')")
  public ResponseEntity<UserResponseDto> updateAllUserFields(
      @RequestBody UserAdminUpdateDto user, @PathVariable String userId) {
    return ResponseEntity.ok(userService.updateAllUserFields(user, userId));
  }

  @PutMapping(value = "/update/profile/{userId}")
  @PreAuthorize("hasAuthority('USER:UPDATE') or #userId == authentication.name")
  public ResponseEntity<UserResponseDto> updateUserProfile(
      @RequestBody UserProfileDto user, @PathVariable String userId) {
    return ResponseEntity.ok(userService.updateUserProfile(user, userId));
  }

  @PutMapping(value = "/update/password/{userId}")
  @PreAuthorize("#userId == authentication.name")
  public ResponseEntity<UserResponseDto> updateUserPassword(
      @RequestBody UserPasswordDto user, @PathVariable String userId) {
    return ResponseEntity.ok(userService.updateUserPassword(user, userId));
  }

  @PutMapping(value = "/update/status/{userId}")
  @PreAuthorize("hasAuthority('USER:UPDATE') or #userId == authentication.name")
  public ResponseEntity<UserResponseDto> updateUserStatus(
      @RequestBody UserStatusUpdateDto userStatusUpdateDto, @PathVariable String userId) {
    return ResponseEntity.ok(userService.updateUserStatusById(userStatusUpdateDto, userId));
  }

  /***  Delete apis ***/
  @DeleteMapping(value = "/delete/{userId}")
  @PreAuthorize("hasAuthority('USER:DELETE') or #userId == authentication.name")
  public void deleteUser(@PathVariable String userId) {
    userService.deleteUser(userId);
  }

  /***  Get apis  ***/
  @GetMapping(value = "/lookup/search/{userId}")
  @PreAuthorize("hasAuthority('USER:READ') or #userId == authentication.name")
  public ResponseEntity<UserResponseDto> getUserById(@PathVariable String userId) {
    return ResponseEntity.ok(userService.getUserById(userId));
  }

  @GetMapping(value = "/lookup/search/email/{email}")
  @PreAuthorize("hasAuthority('USER:READ')")
  public ResponseEntity<UserResponseDto> getUserByEmail(@PathVariable String email) {
    return ResponseEntity.ok(userService.getUserByEmail(email));
  }

  @GetMapping(value = "/lookup/search/all")
  @PreAuthorize("hasAuthority('USER:READ')")
  public ResponseEntity<Iterable<UserResponseDto>> getAllUsers() {
    return ResponseEntity.ok(userService.getAllUsers());
  }
}
