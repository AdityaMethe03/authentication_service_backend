package com.authentication.auth_app_backend.security;

import com.authentication.auth_app_backend.modules.role.RoleRepository;
import com.authentication.auth_app_backend.modules.role.enums.RoleStatusEnum;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthorityResolver {

  private final RoleRepository roleRepository;

  public List<GrantedAuthority> resolve(String tenantId, Set<String> roleIdentifiers) {
    if (roleIdentifiers == null || roleIdentifiers.isEmpty()) {
      return List.of();
    }
    return roleRepository.findByTenantIdAndIdentifierIn(tenantId, roleIdentifiers).stream()
        .filter(role -> role.getStatus() == RoleStatusEnum.ACTIVE)
        .filter(role -> role.getPermissions() != null)
        .flatMap(role -> role.getPermissions().stream())
        .filter(Objects::nonNull)
        .map(permission -> (GrantedAuthority) new SimpleGrantedAuthority(permission.toAuthority()))
        .distinct()
        .toList();
  }
}
