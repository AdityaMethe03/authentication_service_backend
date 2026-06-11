package com.authentication.auth_app_backend.modules.user.dto;

import com.authentication.auth_app_backend.modules.user.enums.Provider;
import com.authentication.auth_app_backend.modules.user.enums.UserStatusEnum;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAdminDto {
  private String id;
  private String email;
  private String name;
  private String password;
  private String image;
  private boolean enable;
  private Provider provider;
  private UserStatusEnum status;
}
