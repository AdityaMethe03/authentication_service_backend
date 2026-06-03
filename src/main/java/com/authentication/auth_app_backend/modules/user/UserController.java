package com.authentication.auth_app_backend.modules.user;

import com.authentication.auth_app_backend.modules.user.dto.UserDto;
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

  @PostMapping(value = "/register")
  public ResponseEntity<UserDto> createUser(@RequestBody UserDto user) {
    user.setId(UUID.randomUUID().toString());
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(user));
  }

  @GetMapping(value = "/lookup/search/all")
  public ResponseEntity<Iterable<UserDto>> getAllUsers() {
    return ResponseEntity.ok(userService.getAllUsers());
  }
}
