package com.authentication.auth_app_backend.dtos;

import com.authentication.auth_app_backend.entities.enums.Provider;
import lombok.*;
import java.util.Date;
import java.util.Set;

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
  private Date createdAt;
  private Date updatedAt;
  private Provider provider;
  private Set<RoleDto> roles;
}
