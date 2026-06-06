package com.authentication.auth_app_backend.modules.user;

import com.authentication.auth_app_backend.common.exceptions.ResourceNotFoundException;
import com.authentication.auth_app_backend.modules.role.RoleRepository;
import com.authentication.auth_app_backend.modules.role.enums.UserRole;
import com.authentication.auth_app_backend.modules.user.dto.UserDto;
import com.authentication.auth_app_backend.modules.user.dto.UserProfileDto;
import com.authentication.auth_app_backend.modules.user.dto.UserResponseDto;
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

    if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
      if (!userDto.getRoles().contains(UserRole.GUEST.name())
          && !userDto.getRoles().contains(UserRole.ADMIN.name())) {
        user.getRoles().add(UserRole.GUEST.name());
      }

      userDto
          .getRoles()
          .forEach(
              role -> {
                roleRepository
                    .findByName(role)
                    .ifPresentOrElse(
                        roleDb -> {
                          user.getRoles().add(roleDb.getName());
                        },
                        () -> {
                          throw new IllegalArgumentException(
                              "Role with given name does not exist.");
                        });
              });
    } else {
      roleRepository
          .findByName(UserRole.GUEST.name())
          .ifPresentOrElse(
              roleDb -> {
                user.setRoles(Set.of(roleDb.getName()));
              },
              () -> {
                throw new IllegalArgumentException(UserRole.GUEST.name() + " role does not exist.");
              });
    }

    User savedUser = userRepository.save(user);

    return modelMapper.map(savedUser, UserResponseDto.class);
  }

  @Override
  public UserResponseDto getUserByEmail(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new ResourceNotFoundException("User with given email does not exist."));
    return modelMapper.map(user, UserResponseDto.class);
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

    return modelMapper.map(user, UserResponseDto.class);
  }

  @Override
  public void deleteUser(String userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with given id does not exist."));
    userRepository.delete(user);
  }

  @Override
  public UserResponseDto getUserById(String userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with given id does not exist."));
    return modelMapper.map(user, UserResponseDto.class);
  }

  @Override
  @Transactional(readOnly = true)
  public Iterable<UserResponseDto> getAllUsers() {
    return userRepository.findAll().stream()
        .map(user -> modelMapper.map(user, UserResponseDto.class))
        .toList();
  }

  @Override
  public UserResponseDto updateUserProfile(UserProfileDto user, String userId) {
    User existingUser =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with given id does not exist."));
    existingUser.setName(user.getName());
    existingUser.setImage(user.getImage());
    existingUser.setUpdatedAt(new Date());

    return modelMapper.map(userRepository.save(existingUser), UserResponseDto.class);
  }
}
