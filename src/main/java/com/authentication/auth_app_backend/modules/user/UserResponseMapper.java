package com.authentication.auth_app_backend.modules.user;

import com.authentication.auth_app_backend.modules.user.dto.UserResponseDto;
import com.authentication.auth_app_backend.security.AuthorityResolver;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserResponseMapper {

  private final ModelMapper modelMapper;
  private final AuthorityResolver authorityResolver;

  public UserResponseDto toResponse(User user) {
    UserResponseDto response = modelMapper.map(user, UserResponseDto.class);
    response.setPermissions(
        authorityResolver.resolve(user.getTenantId(), user.getRoles()).stream()
            .map(authority -> authority.getAuthority())
            .collect(Collectors.toSet()));
    return response;
  }
}
