package com.authentication.auth_app_backend.modules.role;

import com.authentication.auth_app_backend.common.exceptions.ResourceNotFoundException;
import com.authentication.auth_app_backend.modules.role.dto.RoleDto;
import com.authentication.auth_app_backend.modules.role.enums.RoleStatusEnum;
import com.authentication.auth_app_backend.modules.role.enums.UserRole;
import com.authentication.auth_app_backend.modules.user.User;
import com.authentication.auth_app_backend.modules.user.UserRepository;
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

    roleRepository
        .findByName(roleDto.getName())
        .ifPresent(
            role -> {
              throw new IllegalArgumentException("Role with given name already exists.");
            });

    Role role = modelMapper.map(roleDto, Role.class);
    role.setCreatedAt(new Date());
    role.setUpdatedAt(null);

    role.setPermissions(
        roleDto.getPermissions() != null
            ? new HashSet<>(roleDto.getPermissions())
            : new HashSet<>());

    Role savedRole = roleRepository.save(role);

    return modelMapper.map(savedRole, RoleDto.class);
  }

  @Override
  public RoleDto updateRole(RoleDto roleDto, String roleId) {

    if (isSudoAdmin(roleDto.getName())) {
      throw new IllegalArgumentException("Default role with given name cannot be updated.");
    }

    Role existingRole =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with given id"));

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
    Role role =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with given id"));

    if (isSudoAdmin(role.getName())) {
      throw new IllegalArgumentException("Default role with given name cannot be deleted.");
    }

    List<User> users = userRepository.findByRolesContaining(role.getName());
    users.forEach(user -> user.getRoles().remove(role.getName()));
    userRepository.saveAll(users);

    roleRepository.delete(role);
  }

  @Override
  public RoleDto getRoleById(String roleId) {
    Role role =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with given id"));
    return modelMapper.map(role, RoleDto.class);
  }

  @Override
  @Transactional(readOnly = true)
  public Iterable<RoleDto> getAllRoles() {
    return roleRepository.findAll().stream()
        .map(role -> modelMapper.map(role, RoleDto.class))
        .toList();
  }

  @Override
  public RoleDto updateRoleStatusById(RoleStatusEnum status, String roleId) {
    Role existingRole =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with given id"));

    if (isSudoAdmin(existingRole.getName())) {
      throw new IllegalArgumentException("Default role with given name cannot be updated.");
    }

    existingRole.setStatus(status);
    existingRole.setUpdatedAt(new Date());
    Role role = roleRepository.save(existingRole);
    return modelMapper.map(role, RoleDto.class);
  }

  private boolean isSudoAdmin(String name) {
    return UserRole.SUDO_ADMIN.name().equals(name);
  }
}
