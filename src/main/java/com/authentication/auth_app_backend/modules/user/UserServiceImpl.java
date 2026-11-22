package com.authentication.auth_app_backend.modules.user;

import com.authentication.auth_app_backend.common.exceptions.ResourceNotFoundException;
import com.authentication.auth_app_backend.modules.auth.refreshtoken.RefreshTokenRepository;
import com.authentication.auth_app_backend.modules.role.Role;
import com.authentication.auth_app_backend.modules.role.RoleRepository;
import com.authentication.auth_app_backend.modules.role.enums.RoleStatusEnum;
import com.authentication.auth_app_backend.modules.role.enums.UserRole;
import com.authentication.auth_app_backend.modules.tenant.TenantRepository;
import com.authentication.auth_app_backend.modules.tenant.enums.TenantStatusEnum;
import com.authentication.auth_app_backend.modules.user.dto.*;
import com.authentication.auth_app_backend.modules.user.enums.Provider;
import com.authentication.auth_app_backend.security.TenantContext;
import java.util.Date;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final ModelMapper modelMapper;
  private final UserResponseMapper userResponseMapper;
  private final RefreshTokenRepository refreshTokenRepository;
  private final RoleRepository roleRepository;
  private final TenantRepository tenantRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public UserResponseDto createUser(UserDto userDto) {
    String tenantId = TenantContext.getTenantId();
    var tenant =
        tenantRepository
            .findByTenantCodeIgnoreCase(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant was not found."));
    if (tenant.getStatus() != TenantStatusEnum.ACTIVE) {
      throw new IllegalArgumentException("Users cannot be created in an inactive tenant.");
    }
    boolean platformAdmin = TenantContext.isPlatformAdmin();
    if (TenantContext.SYSTEM_TENANT_ID.equals(tenantId) && !platformAdmin) {
      throw new IllegalArgumentException("Only SUDO_ADMIN can create users in SYSTEM.");
    }
    if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
      throw new IllegalArgumentException("Email is required");
    }

    String email = userDto.getEmail().trim().toLowerCase();
    if (TenantContext.SUDO_ADMIN_EMAIL.equalsIgnoreCase(email)) {
      throw new IllegalArgumentException(
          "The reserved SUDO_ADMIN email cannot be registered or assigned to another user.");
    }
    if (userRepository.existsByTenantIdAndEmail(tenantId, email)) {
      throw new IllegalArgumentException("User with given email already exists.");
    }

    User user = modelMapper.map(userDto, User.class);
    user.setTenantId(tenantId);
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(userDto.getPassword()));
    user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);
    user.setEnable(true);
    user.setCreatedAt(new Date());
    user.setUpdatedAt(null);

    if (platformAdmin) {
      if (userDto.getRoles() == null || userDto.getRoles().isEmpty()) {
        throw new IllegalArgumentException("At least one role must be assigned.");
      }
      if (userDto.getRoles().contains(UserRole.SUDO_ADMIN.name())) {
        throw new IllegalArgumentException("SUDO_ADMIN cannot be assigned to users.");
      }
      var selectedRoles =
          roleRepository.findByTenantIdAndIdentifierIn(tenantId, userDto.getRoles());
      var activeRoleIdentifiers =
          selectedRoles.stream()
              .filter(role -> role.getStatus() == RoleStatusEnum.ACTIVE)
              .map(Role::getIdentifier)
              .collect(java.util.stream.Collectors.toSet());
      if (!activeRoleIdentifiers.equals(userDto.getRoles())) {
        throw new IllegalArgumentException("All assigned roles must exist and be active.");
      }
      user.setRoles(activeRoleIdentifiers);
    } else {
      if (userDto.getRoles() != null
          && userDto.getRoles().stream().anyMatch(role -> !UserRole.USER.name().equals(role))) {
        throw new IllegalArgumentException(
            "Only the USER role can be assigned during user registration.");
      }
      Role userRole =
          roleRepository
              .findByTenantIdAndIdentifier(tenantId, UserRole.USER.name())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          UserRole.USER.name() + " role does not exist."));
      if (userRole.getStatus() != RoleStatusEnum.ACTIVE) {
        throw new IllegalArgumentException("The USER role is not active.");
      }
      user.setRoles(Set.of(userRole.getIdentifier()));
    }

    User savedUser = userRepository.save(user);

    return userResponseMapper.toResponse(savedUser);
  }

  @Override
  public UserResponseDto getUserByEmail(String email) {
    String tenantId = TenantContext.getTenantId();
    User user =
        userRepository
            .findByTenantIdAndEmail(tenantId, email.trim().toLowerCase())
            .orElseThrow(
                () -> new ResourceNotFoundException("User with given email does not exist."));
    return userResponseMapper.toResponse(user);
  }

  @Override
  public UserResponseDto updateUser(UserDto userDto, String userId) {
    String tenantId = TenantContext.getTenantId();
    User existingUser =
        userRepository
            .findByTenantIdAndId(tenantId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with given id does not exist."));
    if (existingUser.getRoles() != null
        && existingUser.getRoles().contains(UserRole.SUDO_ADMIN.name())) {
      throw new IllegalArgumentException("The SUDO_ADMIN user's account cannot be changed here.");
    }
    if (TenantContext.SUDO_ADMIN_EMAIL.equalsIgnoreCase(existingUser.getEmail())) {
      throw new IllegalArgumentException(
          "The reserved SUDO_ADMIN user's account cannot be changed.");
    }
    if (!TenantContext.isPlatformAdmin()
        && existingUser.getRoles() != null
        && existingUser.getRoles().contains(UserRole.SYS_ADMIN.name())) {
      throw new IllegalArgumentException("Only SUDO_ADMIN can change a SYS_ADMIN account.");
    }
    existingUser.setName(userDto.getName());
    existingUser.setProvider(userDto.getProvider());
    existingUser.setEnable(userDto.isEnable());
    existingUser.setStatus(userDto.getStatus());
    existingUser.setUpdatedAt(new Date());

    User user = userRepository.save(existingUser);

    return userResponseMapper.toResponse(user);
  }

  @Override
  @Transactional
  public UserResponseDto updateAllUserFields(UserAdminUpdateDto userDto, String userId) {
    String tenantId = TenantContext.getTenantId();
    User existingUser =
        userRepository
            .findByTenantIdAndId(tenantId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with given id does not exist."));

    if (existingUser.getRoles() != null
        && existingUser.getRoles().contains(UserRole.SUDO_ADMIN.name())) {
      throw new IllegalArgumentException("The SUDO_ADMIN user's account cannot be changed.");
    }
    if (TenantContext.SUDO_ADMIN_EMAIL.equalsIgnoreCase(existingUser.getEmail())) {
      throw new IllegalArgumentException(
          "The reserved SUDO_ADMIN user's account cannot be changed.");
    }
    if (userDto.getRoles() == null || userDto.getRoles().isEmpty()) {
      throw new IllegalArgumentException("At least one role must be assigned.");
    }
    if (userDto.getRoles().contains(UserRole.SUDO_ADMIN.name())) {
      throw new IllegalArgumentException("SUDO_ADMIN cannot be assigned to users.");
    }
    if (userDto.getRoles().contains(UserRole.SYS_ADMIN.name())
        && !TenantContext.isPlatformAdmin()) {
      throw new IllegalArgumentException("Only SUDO_ADMIN can assign SYS_ADMIN.");
    }
    if (!TenantContext.isPlatformAdmin()
        && existingUser.getRoles() != null
        && existingUser.getRoles().contains(UserRole.SYS_ADMIN.name())
        && !userDto.getRoles().contains(UserRole.SYS_ADMIN.name())) {
      throw new IllegalArgumentException("Only SUDO_ADMIN can change a SYS_ADMIN account's role.");
    }

    var roles = roleRepository.findByTenantIdAndIdentifierIn(tenantId, userDto.getRoles());
    var activeRoleNames =
        roles.stream()
            .filter(role -> role.getStatus() == RoleStatusEnum.ACTIVE)
            .map(Role::getIdentifier)
            .collect(java.util.stream.Collectors.toSet());
    if (!activeRoleNames.equals(userDto.getRoles())) {
      throw new IllegalArgumentException("All assigned roles must exist and be active.");
    }

    existingUser.setName(userDto.getName());
    existingUser.setEnable(userDto.isEnable());
    existingUser.setStatus(userDto.getStatus());
    existingUser.setRoles(activeRoleNames);
    existingUser.setUpdatedAt(new Date());
    return userResponseMapper.toResponse(userRepository.save(existingUser));
  }

  @Override
  @Transactional
  public void deleteUser(String userId) {
    String tenantId = TenantContext.getTenantId();
    User user =
        userRepository
            .findByTenantIdAndId(tenantId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with given id does not exist."));
    if (user.getRoles() != null && user.getRoles().contains(UserRole.SUDO_ADMIN.name())) {
      throw new IllegalArgumentException("The SUDO_ADMIN account cannot be deleted.");
    }
    if (TenantContext.SUDO_ADMIN_EMAIL.equalsIgnoreCase(user.getEmail())) {
      throw new IllegalArgumentException("The reserved SUDO_ADMIN account cannot be deleted.");
    }
    if (!TenantContext.isPlatformAdmin()
        && user.getRoles() != null
        && user.getRoles().contains(UserRole.SYS_ADMIN.name())) {
      throw new IllegalArgumentException("Only SUDO_ADMIN can delete a SYS_ADMIN account.");
    }
    refreshTokenRepository.deleteByTenantIdAndUserId(tenantId, userId);
    userRepository.delete(user);
  }

  @Override
  public UserResponseDto getUserById(String userId) {
    String tenantId = TenantContext.getTenantId();
    User user =
        userRepository
            .findByTenantIdAndId(tenantId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with given id does not exist."));
    return userResponseMapper.toResponse(user);
  }

  @Override
  @Transactional(readOnly = true)
  public Iterable<UserResponseDto> getAllUsers() {
    String tenantId = TenantContext.getTenantId();
    var users =
        TenantContext.SYSTEM_TENANT_ID.equals(tenantId) && TenantContext.isPlatformAdmin()
            ? userRepository.findAll()
            : userRepository.findAllByTenantId(tenantId);
    return users.stream().map(userResponseMapper::toResponse).toList();
  }

  @Override
  public UserResponseDto updateUserProfile(UserProfileDto userDto, String userId) {
    String tenantId = TenantContext.getTenantId();
    User existingUser =
        userRepository
            .findByTenantIdAndId(tenantId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with given id does not exist."));
    existingUser.setName(userDto.getName());
    existingUser.setImage(userDto.getImage());
    existingUser.setUpdatedAt(new Date());

    User user = userRepository.save(existingUser);
    return userResponseMapper.toResponse(user);
  }

  @Override
  public UserResponseDto updateUserPassword(UserPasswordDto userDto, String userId) {
    String tenantId = TenantContext.getTenantId();
    User existingUser =
        userRepository
            .findByTenantIdAndId(tenantId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with given id does not exist."));
    if (userDto.getOldPassword().isBlank() || userDto.getOldPassword().isEmpty()) {
      throw new IllegalArgumentException("Password is required");
    }
    if (!passwordEncoder.matches(userDto.getOldPassword(), existingUser.getPassword())) {
      throw new IllegalArgumentException("Old password is incorrect");
    }
    if (userDto.getOldPassword().equals(userDto.getNewPassword())) {
      throw new IllegalArgumentException("New password cannot be same as old password");
    }

    existingUser.setPassword(passwordEncoder.encode(userDto.getNewPassword()));
    existingUser.setUpdatedAt(new Date());

    User user = userRepository.save(existingUser);
    return userResponseMapper.toResponse(user);
  }

  @Override
  public UserResponseDto updateUserStatusById(
      UserStatusUpdateDto userStatusUpdateDto, String userId) {
    String tenantId = TenantContext.getTenantId();
    User existingUser =
        userRepository
            .findByTenantIdAndId(tenantId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with given id does not exist."));
    existingUser.setStatus(userStatusUpdateDto.getStatus());
    existingUser.setUpdatedAt(new Date());

    User user = userRepository.save(existingUser);
    return userResponseMapper.toResponse(user);
  }

  @Override
  @Transactional
  public UserResponseDto createAdminUser(UserAdminDto userDto) {
    String tenantId = TenantContext.getTenantId();
    if (!TenantContext.isPlatformAdmin()) {
      String actorId = SecurityContextHolder.getContext().getAuthentication().getName();
      User actor =
          userRepository
              .findByTenantIdAndId(tenantId, actorId)
              .orElseThrow(() -> new IllegalArgumentException("Only SYS_ADMIN can create admins."));
      if (actor.getRoles() == null || !actor.getRoles().contains(UserRole.SYS_ADMIN.name())) {
        throw new IllegalArgumentException("Only SYS_ADMIN can create admins.");
      }
    }
    if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
      throw new IllegalArgumentException("Email is required");
    }

    String email = userDto.getEmail().trim().toLowerCase();
    if (TenantContext.SUDO_ADMIN_EMAIL.equalsIgnoreCase(email)) {
      throw new IllegalArgumentException(
          "The reserved SUDO_ADMIN email cannot be registered or assigned to another user.");
    }
    if (userRepository.existsByTenantIdAndEmail(tenantId, email)) {
      throw new IllegalArgumentException("User with given email already exists.");
    }

    Role role =
        roleRepository
            .findByTenantIdAndIdentifier(tenantId, UserRole.ADMIN.name())
            .orElseThrow(() -> new ResourceNotFoundException("Admin role does not exist."));

    User user = modelMapper.map(userDto, User.class);
    user.setTenantId(tenantId);
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(userDto.getPassword()));
    user.setEnable(true);
    user.setCreatedAt(new Date());
    user.setUpdatedAt(null);
    user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);
    user.setRoles(Set.of(role.getIdentifier()));

    User savedUser = userRepository.save(user);

    return userResponseMapper.toResponse(savedUser);
  }
}
