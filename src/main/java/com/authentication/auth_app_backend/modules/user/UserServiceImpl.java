package com.authentication.auth_app_backend.modules.user;

import com.authentication.auth_app_backend.modules.role.RoleRepository;
import com.authentication.auth_app_backend.modules.role.enums.UserRole;
import com.authentication.auth_app_backend.modules.user.dto.UserDto;
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
  public UserDto createUser(UserDto userDto) {
    if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
      throw new IllegalArgumentException("Email is required");
    }

    if (userRepository.existsByEmail(userDto.getEmail())) {
      throw new IllegalArgumentException("User with given email already exists.");
    }

    User user = modelMapper.map(userDto, User.class);
    user.setPassword(passwordEncoder.encode(userDto.getPassword()));
    user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);
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
                          user.setRoles(Set.of(roleDb.getName()));
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

    return modelMapper.map(savedUser, UserDto.class);
  }

  @Override
  public UserDto getUserByEmail(String email) {
    return null;
  }

  @Override
  public UserDto updateUser(UserDto user, String userId) {
    return null;
  }

  @Override
  public void deleteUser(String userId) {}

  @Override
  public UserDto getUserById(String userId) {
    return null;
  }

  @Override
  @Transactional(readOnly = true)
  public Iterable<UserDto> getAllUsers() {
    return userRepository.findAll().stream()
        .map(user -> modelMapper.map(user, UserDto.class))
        .toList();
  }
}
