package com.authentication.auth_app_backend.modules.role;

import com.authentication.auth_app_backend.modules.role.dto.RoleDto;

public interface RoleService {

  // Create role
  RoleDto createRole(RoleDto role);

  // update role
  RoleDto updateRole(RoleDto role, String roleId);

  // delete role
  void deleteRole(String roleId);

  // get a role by id
  RoleDto getRoleById(String roleId);

  // get all roles
  Iterable<RoleDto> getAllRoles();
}
