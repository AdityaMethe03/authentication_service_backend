package com.authentication.auth_app_backend.modules.user.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPasswordDto {
  private String password;
  private String confirmPassword;
}
