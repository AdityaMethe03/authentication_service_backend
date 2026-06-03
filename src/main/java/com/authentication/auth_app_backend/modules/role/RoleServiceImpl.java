package com.authentication.auth_app_backend.modules.role;

import com.authentication.auth_app_backend.common.exceptions.ResourceNotFoundException;
import com.authentication.auth_app_backend.modules.role.dto.RoleDto;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
  private final ModelMapper modelMapper;
  private final RoleRepository roleRepository;

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

    Role savedRole = roleRepository.save(role);

    return modelMapper.map(savedRole, RoleDto.class);
  }

  @Override
  public RoleDto updateRole(RoleDto roleDto, String roleId) {

    Role existingRole =
        roleRepository
            .findById(roleDto.getName())
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with given id"));
    existingRole.setName(roleDto.getName());
    existingRole.setStatus(roleDto.getStatus());
    existingRole.setUpdatedAt(new Date());

    Role role = roleRepository.save(existingRole);

    return modelMapper.map(role, RoleDto.class);
  }

  @Override
  public void deleteRole(String roleId) {
    Role role =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with given id"));
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
}
