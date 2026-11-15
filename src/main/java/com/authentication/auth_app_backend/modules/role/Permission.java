package com.authentication.auth_app_backend.modules.role;

import com.authentication.auth_app_backend.modules.role.enums.ApplicationEntityType;
import com.authentication.auth_app_backend.modules.role.enums.PermissionType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Permission {

  private ApplicationEntityType entity;
  private PermissionType action;

  public String toAuthority() {
    return entity.name() + ":" + action.name(); // e.g. "PROJECT:READ"
  }
}
