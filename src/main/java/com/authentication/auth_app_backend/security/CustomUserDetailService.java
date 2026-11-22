package com.authentication.auth_app_backend.security;

import com.authentication.auth_app_backend.modules.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    String tenantId = TenantContext.getTenantId();
    String normalizedEmail = username.trim().toLowerCase();
    if (TenantContext.SUDO_ADMIN_EMAIL.equalsIgnoreCase(normalizedEmail)
        && !TenantContext.SYSTEM_TENANT_ID.equals(tenantId)) {
      throw new BadCredentialsException("Invalid Email or Password!!");
    }
    return userRepository
        .findByTenantIdAndEmail(tenantId, normalizedEmail)
        .orElseThrow(() -> new BadCredentialsException("Invalid Email or Password!!"));
  }
}
