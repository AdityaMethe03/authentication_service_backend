package com.authentication.auth_app_backend.modules.role;

import com.authentication.auth_app_backend.common.exceptions.ResourceNotFoundException;
import com.authentication.auth_app_backend.modules.role.dto.RoleDto;
import com.authentication.auth_app_backend.modules.role.enums.RoleStatusEnum;
import com.authentication.auth_app_backend.modules.role.enums.UserRole;
import com.authentication.auth_app_backend.modules.user.User;
import com.authentication.auth_app_backend.modules.user.UserRepository;
import com.authentication.auth_app_backend.security.TenantContext;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
  private final ModelMapper modelMapper;
  private final RoleRepository roleRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public RoleDto createRole(RoleDto roleDto) {
    String tenantId = TenantContext.getTenantId();

    if (roleDto.getIdentifier() == null || roleDto.getIdentifier().isBlank()) {
      throw new IllegalArgumentException("Role identifier is required.");
    }
    if (!roleDto.getIdentifier().matches("[A-Z][A-Z0-9_]*")) {
      throw new IllegalArgumentException(
          "Role identifier must start with an uppercase letter and contain only uppercase letters, numbers, and underscores.");
    }
    validateSudoAdminRoleScope(tenantId, roleDto.getIdentifier());
    if (roleRepository.existsByTenantIdAndIdentifier(tenantId, roleDto.getIdentifier())) {
      throw new IllegalArgumentException("Role identifier already exists.");
    }

    roleRepository
        .findByTenantIdAndName(tenantId, roleDto.getName())
        .ifPresent(
            role -> {
              throw new IllegalArgumentException("Role with given name already exists.");
            });

    Role role = modelMapper.map(roleDto, Role.class);
    role.setTenantId(tenantId);
    role.setCreatedAt(new Date());
    role.setUpdatedAt(null);

    role.setPermissions(
        roleDto.getPermissions() != null
            ? new HashSet<>(roleDto.getPermissions())
            : new HashSet<>());
    role.setIsSystem(false);

    Role savedRole = roleRepository.save(role);

    return modelMapper.map(savedRole, RoleDto.class);
  }

  @Override
  public RoleDto updateRole(RoleDto roleDto, String roleId) {
    String tenantId = TenantContext.getTenantId();
    Role existingRole =
        roleRepository
            .findByTenantIdAndId(tenantId, roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with given id"));

    validateSudoAdminRoleScope(tenantId, roleDto.getIdentifier(), existingRole.getIdentifier());

    if ("SUDO_ADMIN".equals(existingRole.getIdentifier())) {
      throw new IllegalArgumentException("The SUDO_ADMIN role cannot be changed.");
    }
    if (Boolean.TRUE.equals(existingRole.getIsSystem())
        && (!java.util.Objects.equals(existingRole.getName(), roleDto.getName())
            || !java.util.Objects.equals(existingRole.getStatus(), roleDto.getStatus()))) {
      throw new IllegalArgumentException("The name and status of system roles cannot be changed.");
    }

    roleRepository
        .findByTenantIdAndName(tenantId, roleDto.getName())
        .filter(role -> !role.getId().equals(roleId))
        .ifPresent(
            role -> {
              throw new IllegalArgumentException("Role with given name already exists.");
            });

    existingRole.setName(roleDto.getName());
    existingRole.setStatus(roleDto.getStatus());
    existingRole.setPermissions(
        roleDto.getPermissions() != null
            ? new HashSet<>(roleDto.getPermissions())
            : existingRole.getPermissions());
    existingRole.setUpdatedAt(new Date());

    Role role = roleRepository.save(existingRole);
    return modelMapper.map(role, RoleDto.class);
  }

  @Override
  @Transactional
  public void deleteRole(String roleId) {
    String tenantId = TenantContext.getTenantId();
    Role role =
        roleRepository
            .findByTenantIdAndId(tenantId, roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with given id"));

    validateSudoAdminRoleScope(tenantId, role.getIdentifier());

    if (Boolean.TRUE.equals(role.getIsSystem())) {
      throw new IllegalArgumentException("System roles cannot be deleted.");
    }

    List<User> users =
        userRepository.findByTenantIdAndRolesContaining(tenantId, role.getIdentifier());
    users.forEach(user -> user.getRoles().remove(role.getIdentifier()));
    userRepository.saveAll(users);

    roleRepository.delete(role);
  }

  @Override
  public RoleDto getRoleById(String roleId) {
    String tenantId = TenantContext.getTenantId();
    Role role =
        roleRepository
            .findByTenantIdAndId(tenantId, roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with given id"));
    return modelMapper.map(role, RoleDto.class);
  }

  @Override
  @Transactional(readOnly = true)
  public Iterable<RoleDto> getAllRoles() {
    String tenantId = TenantContext.getTenantId();
    var roles =
        TenantContext.SYSTEM_TENANT_ID.equals(tenantId) && TenantContext.isPlatformAdmin()
            ? roleRepository.findAll()
            : roleRepository.findAllByTenantId(tenantId);
    return roles.stream().map(role -> modelMapper.map(role, RoleDto.class)).toList();
  }

  @Override
  public RoleDto updateRoleStatusById(RoleStatusEnum status, String roleId) {
    String tenantId = TenantContext.getTenantId();
    Role existingRole =
        roleRepository
            .findByTenantIdAndId(tenantId, roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with given id"));

    validateSudoAdminRoleScope(tenantId, existingRole.getIdentifier());

    if (Boolean.TRUE.equals(existingRole.getIsSystem())) {
      throw new IllegalArgumentException("The status of system roles cannot be changed.");
    }

    existingRole.setStatus(status);
    existingRole.setUpdatedAt(new Date());
    Role role = roleRepository.save(existingRole);
    return modelMapper.map(role, RoleDto.class);
  }

  private static void validateSudoAdminRoleScope(String tenantId, String... roleValues) {
    boolean sudoAdminRole =
        java.util.Arrays.stream(roleValues)
            .filter(java.util.Objects::nonNull)
            .anyMatch(value -> UserRole.SUDO_ADMIN.name().equalsIgnoreCase(value.trim()));
    if (sudoAdminRole && !TenantContext.SYSTEM_TENANT_ID.equals(tenantId)) {
      throw new IllegalArgumentException(
          "The SUDO_ADMIN role can only exist in the SYSTEM tenant.");
    }
  }
}
