package com.authentication.auth_app_backend.modules.user.dto;

import com.authentication.auth_app_backend.modules.user.enums.UserStatusEnum;
import java.util.Set;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAdminUpdateDto {
  private String name;
  private boolean enable;
  private UserStatusEnum status;
  private Set<String> roles;
}
