package com.authentication.auth_app_backend.modules.role.dto;

import com.authentication.auth_app_backend.modules.role.Permission;
import com.authentication.auth_app_backend.modules.role.enums.RoleStatusEnum;
import java.util.Date;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoleDto {
  private String id;
  private String identifier;
  private String name;
  private Boolean isSystem;
  private RoleStatusEnum status;
  private Set<Permission> permissions;
  private Date createdAt;
  private Date updatedAt;
}
