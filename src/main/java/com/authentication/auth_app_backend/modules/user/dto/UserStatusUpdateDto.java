package com.authentication.auth_app_backend.modules.user.dto;

import com.authentication.auth_app_backend.modules.user.enums.UserStatusEnum;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserStatusUpdateDto {
  private UserStatusEnum status;
}
