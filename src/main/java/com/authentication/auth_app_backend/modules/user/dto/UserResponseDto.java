package com.authentication.auth_app_backend.modules.user.dto;

import com.authentication.auth_app_backend.modules.user.enums.Provider;
import com.authentication.auth_app_backend.modules.user.enums.UserStatusEnum;
import lombok.*;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDto {
  private String id;
  private String email;
  private String name;
  private String image;
  private boolean enable;
  private Date createdAt;
  private Date updatedAt;
  private Provider provider;
  private Set<String> roles;
  private UserStatusEnum status;
}
