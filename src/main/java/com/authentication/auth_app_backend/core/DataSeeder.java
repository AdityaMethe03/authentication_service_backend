package com.authentication.auth_app_backend.core;

import com.authentication.auth_app_backend.modules.role.Permission;
import com.authentication.auth_app_backend.modules.role.Role;
import com.authentication.auth_app_backend.modules.role.RoleRepository;
import com.authentication.auth_app_backend.modules.role.enums.ApplicationEntityType;
import com.authentication.auth_app_backend.modules.role.enums.PermissionType;
import com.authentication.auth_app_backend.modules.role.enums.RoleStatusEnum;
import com.authentication.auth_app_backend.modules.role.enums.UserRole;
import com.authentication.auth_app_backend.modules.tenant.Tenant;
import com.authentication.auth_app_backend.modules.tenant.TenantRepository;
import com.authentication.auth_app_backend.modules.tenant.enums.TenantStatusEnum;
import com.authentication.auth_app_backend.modules.user.User;
import com.authentication.auth_app_backend.modules.user.UserRepository;
import com.authentication.auth_app_backend.modules.user.enums.Provider;
import com.authentication.auth_app_backend.modules.user.enums.UserStatusEnum;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

  private final RoleRepository roleRepository;
  private final TenantRepository tenantRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Value("${app.sudo-admin.email}")
  private String sudoAdminEmail;

  @Value("${app.sudo-admin.password}")
  private String sudoAdminPassword;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    seedSystemTenant();
    seedDefaultRoles();
    seedSudoAdmin();
  }

  private void seedSystemTenant() {
    tenantRepository
        .findByTenantCodeIgnoreCase("SYSTEM")
        .orElseGet(
            () ->
                tenantRepository.save(
                    Tenant.builder()
                        .id(UUID.randomUUID().toString())
                        .tenantCode("SYSTEM")
                        .name("System")
                        .status(TenantStatusEnum.ACTIVE)
                        .createdAt(new Date())
                        .build()));
  }

  private void seedDefaultRoles() {
    seedRole(UserRole.SUDO_ADMIN.name(), allPermissions(), true);
  }

  private void seedRole(String name, Set<Permission> defaults, boolean enforce) {
    Role role = roleRepository.findByTenantIdAndIdentifier("SYSTEM", name).orElse(null);

    if (role == null) {
      role = new Role();
      role.setId(UUID.randomUUID().toString());
      role.setTenantId("SYSTEM");
      role.setIdentifier(name);
      role.setName(name);
      role.setIsSystem(true);
      role.setStatus(RoleStatusEnum.ACTIVE);
      role.setPermissions(new HashSet<>(defaults));
      role.setCreatedAt(new Date());
      roleRepository.save(role);
      log.info("{} role created with {} permissions.", name, defaults.size());
      return;
    }

    if (role.getPermissions() == null) {
      role.setPermissions(new HashSet<>());
    }
    boolean metadataChanged = false;
    if (role.getIdentifier() == null || role.getIdentifier().isBlank()) {
      role.setIdentifier(name);
      metadataChanged = true;
    }
    if (!Boolean.TRUE.equals(role.getIsSystem())) {
      role.setIsSystem(true);
      metadataChanged = true;
    }
    if (enforce && !role.getPermissions().containsAll(defaults)) {
      role.getPermissions().addAll(defaults);
      roleRepository.save(role);
      log.info("{} role synced with default permissions.", name);
    } else if (metadataChanged) {
      roleRepository.save(role);
    } else {
      log.info("{} role already exists.", name);
    }
  }

  private static Set<Permission> allPermissions() {
    Set<Permission> all = new HashSet<>();
    for (ApplicationEntityType entity : ApplicationEntityType.values()) {
      for (PermissionType action : PermissionType.values()) {
        all.add(new Permission(entity, action));
      }
    }
    return all;
  }

  private void seedSudoAdmin() {
    userRepository
        .findByTenantIdAndEmail("SYSTEM", sudoAdminEmail.trim().toLowerCase())
        .ifPresentOrElse(
            user -> log.info("Sudo admin already exists: {}", user.getEmail()),
            () -> {
              User user =
                  User.builder()
                      .id(sudoAdminEmail)
                      .tenantId("SYSTEM")
                      .email(sudoAdminEmail.trim().toLowerCase())
                      .name("Sudo Admin")
                      .password(passwordEncoder.encode(sudoAdminPassword))
                      .enable(true)
                      .provider(Provider.LOCAL)
                      .roles(Set.of(UserRole.SUDO_ADMIN.name()))
                      .status(UserStatusEnum.ACTIVE)
                      .createdAt(new Date())
                      .build();
              userRepository.save(user);
              log.info("Sudo admin created: {}", sudoAdminEmail);
            });
  }
}
