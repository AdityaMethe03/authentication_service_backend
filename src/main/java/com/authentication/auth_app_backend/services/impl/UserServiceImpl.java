package com.authentication.auth_app_backend.services.impl;

import com.authentication.auth_app_backend.dtos.UserDto;
import com.authentication.auth_app_backend.entities.User;
import com.authentication.auth_app_backend.entities.enums.Provider;
import com.authentication.auth_app_backend.repositories.UserRepository;
import com.authentication.auth_app_backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

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
        user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);
        user.setCreatedAt(new Date());

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
    public void deleteUser(String userId) {

    }

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
