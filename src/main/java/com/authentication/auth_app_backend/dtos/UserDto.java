package com.authentication.auth_app_backend.dtos;

import com.authentication.auth_app_backend.entities.enums.Provider;
import java.time.Instant;
import java.util.Set;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
  private String id;
  private String email;
  private String name;
  private String password;
  private String image;
  private boolean enable;
  private Instant createdAt;
  private Instant updatedAt;
  private Provider provider;
  private Set<RoleDto> roles;
}
