package com.authentication.auth_app_backend.modules.user;

import com.authentication.auth_app_backend.common.exceptions.ResourceNotFoundException;
import com.authentication.auth_app_backend.modules.auth.refreshtoken.RefreshTokenRepository;
import com.authentication.auth_app_backend.modules.role.Role;
import com.authentication.auth_app_backend.modules.role.RoleRepository;
import com.authentication.auth_app_backend.modules.role.enums.RoleStatusEnum;
import com.authentication.auth_app_backend.modules.role.enums.UserRole;
import com.authentication.auth_app_backend.modules.user.dto.*;
import com.authentication.auth_app_backend.modules.user.enums.Provider;
import java.util.Date;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public UserResponseDto createUser(UserDto userDto) {
    if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
      throw new IllegalArgumentException("Email is required");
    }

    if (userRepository.existsByEmail(userDto.getEmail())) {
      throw new IllegalArgumentException("User with given email already exists.");
    }

    User user = modelMapper.map(userDto, User.class);
    user.setPassword(passwordEncoder.encode(userDto.getPassword()));
    user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);
    user.setEnable(true);
    user.setCreatedAt(new Date());
    user.setUpdatedAt(null);

    if (userDto.getRoles() != null
        && userDto.getRoles().stream().anyMatch(role -> !UserRole.GUEST.name().equals(role))) {
      throw new IllegalArgumentException(
          "Only the GUEST role can be assigned during user registration.");
    }

    Role guestRole =
        roleRepository
            .findByIdentifier(UserRole.GUEST.name())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(UserRole.GUEST.name() + " role does not exist."));
    if (guestRole.getStatus() != RoleStatusEnum.ACTIVE) {
      throw new IllegalArgumentException("The GUEST role is not active.");
    }
    user.setRoles(Set.of(guestRole.getIdentifier()));

    User savedUser = userRepository.save(user);

    return userResponseMapper.toResponse(savedUser);
  }

  @Override
  public UserResponseDto getUserByEmail(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new ResourceNotFoundException("User with given email does not exist."));
    return userResponseMapper.toResponse(user);
  }

  @Override
  public UserResponseDto updateUser(UserDto userDto, String userId) {
    User existingUser =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with given id does not exist."));
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
    User existingUser =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with given id does not exist."));

    if (existingUser.getRoles() != null
        && existingUser.getRoles().contains(UserRole.SUDO_ADMIN.name())) {
      throw new IllegalArgumentException("The SUDO_ADMIN user's account cannot be changed.");
    }
    if (userDto.getRoles() == null || userDto.getRoles().isEmpty()) {
      throw new IllegalArgumentException("At least one role must be assigned.");
    }
    if (userDto.getRoles().contains(UserRole.SUDO_ADMIN.name())) {
      throw new IllegalArgumentException("SUDO_ADMIN cannot be assigned to users.");
    }

    var roles = roleRepository.findByIdentifierIn(userDto.getRoles());
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
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with given id does not exist."));
    refreshTokenRepository.deleteByUserId(userId);
    userRepository.delete(user);
  }

  @Override
  public UserResponseDto getUserById(String userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with given id does not exist."));
    return userResponseMapper.toResponse(user);
  }

  @Override
  @Transactional(readOnly = true)
  public Iterable<UserResponseDto> getAllUsers() {
    return userRepository.findAll().stream().map(userResponseMapper::toResponse).toList();
  }

  @Override
  public UserResponseDto updateUserProfile(UserProfileDto userDto, String userId) {
    User existingUser =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with given id does not exist."));
    existingUser.setName(userDto.getName());
    existingUser.setImage(userDto.getImage());
    existingUser.setUpdatedAt(new Date());

    User user = userRepository.save(existingUser);
    return userResponseMapper.toResponse(user);
  }

  @Override
  public UserResponseDto updateUserPassword(UserPasswordDto userDto, String userId) {
    User existingUser =
        userRepository
            .findById(userId)
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
    User existingUser =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with given id does not exist."));
    existingUser.setStatus(userStatusUpdateDto.getStatus());
    existingUser.setUpdatedAt(new Date());

    User user = userRepository.save(existingUser);
    return userResponseMapper.toResponse(user);
  }

  @Override
  @Transactional
  public UserResponseDto createAdminUser(UserAdminDto userDto) {
    if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
      throw new IllegalArgumentException("Email is required");
    }

    if (userRepository.existsByEmail(userDto.getEmail())) {
      throw new IllegalArgumentException("User with given email already exists.");
    }

    Role role =
        roleRepository
            .findByIdentifier(UserRole.ADMIN.name())
            .orElseThrow(() -> new ResourceNotFoundException("Admin role does not exist."));

    User user = modelMapper.map(userDto, User.class);
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
