package com.authentication.auth_app_backend.modules.tenant;

import com.authentication.auth_app_backend.common.exceptions.ResourceNotFoundException;
import com.authentication.auth_app_backend.modules.role.Permission;
import com.authentication.auth_app_backend.modules.role.Role;
import com.authentication.auth_app_backend.modules.role.RoleRepository;
import com.authentication.auth_app_backend.modules.role.enums.ApplicationEntityType;
import com.authentication.auth_app_backend.modules.role.enums.PermissionType;
import com.authentication.auth_app_backend.modules.role.enums.RoleStatusEnum;
import com.authentication.auth_app_backend.modules.role.enums.UserRole;
import com.authentication.auth_app_backend.modules.tenant.dto.TenantRegistrationDto;
import com.authentication.auth_app_backend.modules.tenant.dto.TenantResponseDto;
import com.authentication.auth_app_backend.modules.tenant.dto.TenantUpdateDto;
import com.authentication.auth_app_backend.modules.tenant.enums.TenantStatusEnum;
import com.authentication.auth_app_backend.security.TenantContext;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {
  private final TenantRepository tenantRepository;
  private final RoleRepository roleRepository;

  @Override
  @Transactional
  public TenantResponseDto registerTenant(TenantRegistrationDto registration) {
    if (!TenantContext.SYSTEM_TENANT_ID.equals(TenantContext.getTenantId())) {
      throw new IllegalArgumentException("Tenants can only be created from the SYSTEM scope.");
    }
    if (!TenantContext.isPlatformAdmin()) {
      throw new IllegalArgumentException("Only SUDO_ADMIN can create a tenant.");
    }

    String tenantCode = registration.getTenantCode().trim().toLowerCase();
    if (!tenantCode.matches("[a-z0-9][a-z0-9-]{1,48}[a-z0-9]")) {
      throw new IllegalArgumentException(
          "Tenant code must be 3–50 characters and contain only lowercase letters, numbers, and hyphens.");
    }
    if (TenantContext.SYSTEM_TENANT_ID.equalsIgnoreCase(tenantCode)) {
      throw new IllegalArgumentException("SYSTEM is a reserved tenant code.");
    }
    if (tenantRepository.existsByTenantCodeIgnoreCase(tenantCode)) {
      throw new IllegalArgumentException("Tenant code already exists.");
    }

    // The UUID identifies the tenant document; the immutable code scopes tenant-owned data.
    String tenantId = tenantCode;
    Tenant tenant =
        Tenant.builder()
            .id(UUID.randomUUID().toString())
            .tenantCode(tenantCode)
            .name(registration.getName().trim())
            .contactPerson(registration.getContactPerson().trim())
            .contactEmail(registration.getContactEmail().trim().toLowerCase())
            .contactNumber(registration.getContactNumber().trim())
            .status(TenantStatusEnum.ACTIVE)
            .createdAt(new Date())
            .build();
    tenantRepository.save(tenant);

    seedTenantRoles(tenantId);

    return TenantResponseDto.builder()
        .id(tenant.getId())
        .tenantCode(tenant.getTenantCode())
        .name(tenant.getName())
        .contactPerson(tenant.getContactPerson())
        .contactEmail(tenant.getContactEmail())
        .contactNumber(tenant.getContactNumber())
        .status(tenant.getStatus())
        .createdAt(tenant.getCreatedAt())
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public Iterable<TenantResponseDto> getAllTenants() {
    if (!TenantContext.SYSTEM_TENANT_ID.equals(TenantContext.getTenantId())) {
      throw new IllegalArgumentException("Tenants can only be listed from the SYSTEM scope.");
    }
    return tenantRepository.findAllByOrderByNameAsc().stream()
        .map(
            tenant ->
                TenantResponseDto.builder()
                    .id(tenant.getId())
                    .tenantCode(tenant.getTenantCode())
                    .name(tenant.getName())
                    .contactPerson(tenant.getContactPerson())
                    .contactEmail(tenant.getContactEmail())
                    .contactNumber(tenant.getContactNumber())
                    .status(tenant.getStatus())
                    .createdAt(tenant.getCreatedAt())
                    .build())
        .toList();
  }

  @Override
  @Transactional
  public TenantResponseDto updateTenant(TenantUpdateDto update, String tenantCode) {
    if (!TenantContext.SYSTEM_TENANT_ID.equals(TenantContext.getTenantId())
        || !TenantContext.isPlatformAdmin()) {
      throw new IllegalArgumentException("Only SUDO_ADMIN in SYSTEM can update tenants.");
    }
    Tenant tenant =
        tenantRepository
            .findByTenantCodeIgnoreCase(tenantCode)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant was not found."));
    if (TenantContext.SYSTEM_TENANT_ID.equals(tenant.getTenantCode())) {
      throw new IllegalArgumentException("The SYSTEM tenant cannot be changed.");
    }
    tenant.setName(update.getName().trim());
    tenant.setStatus(update.getStatus());
    tenantRepository.save(tenant);
    return TenantResponseDto.builder()
        .id(tenant.getId())
        .tenantCode(tenant.getTenantCode())
        .name(tenant.getName())
        .contactPerson(tenant.getContactPerson())
        .contactEmail(tenant.getContactEmail())
        .contactNumber(tenant.getContactNumber())
        .status(tenant.getStatus())
        .createdAt(tenant.getCreatedAt())
        .build();
  }

  private void seedTenantRoles(String tenantId) {
    createSystemRole(
        tenantId,
        UserRole.SYS_ADMIN.name(),
        permissions(
            ApplicationEntityType.USER,
            PermissionType.values(),
            ApplicationEntityType.ROLE,
            PermissionType.values()));
    createSystemRole(
        tenantId,
        UserRole.ADMIN.name(),
        permissions(
            ApplicationEntityType.USER,
            PermissionType.READ,
            PermissionType.WRITE,
            PermissionType.UPDATE,
            ApplicationEntityType.ROLE,
            PermissionType.READ,
            PermissionType.UPDATE));
    createSystemRole(tenantId, UserRole.USER.name(), Set.of());
  }

  private void createSystemRole(String tenantId, String identifier, Set<Permission> permissions) {
    if (roleRepository.findByTenantIdAndIdentifier(tenantId, identifier).isPresent()) {
      return;
    }
    Role role = new Role();
    role.setId(UUID.randomUUID().toString());
    role.setTenantId(tenantId);
    role.setIdentifier(identifier);
    role.setName(identifier);
    role.setIsSystem(true);
    role.setStatus(RoleStatusEnum.ACTIVE);
    role.setPermissions(new HashSet<>(permissions));
    role.setCreatedAt(new Date());
    roleRepository.save(role);
  }

  private static Set<Permission> permissions(Object... values) {
    Set<Permission> permissions = new HashSet<>();
    ApplicationEntityType entity = null;
    for (Object value : values) {
      if (value instanceof ApplicationEntityType nextEntity) {
        entity = nextEntity;
      } else if (entity != null && value instanceof PermissionType action) {
        permissions.add(new Permission(entity, action));
      } else if (entity != null && value instanceof PermissionType[] actions) {
        for (PermissionType action : actions) permissions.add(new Permission(entity, action));
      }
    }
    return permissions.stream().collect(Collectors.toSet());
  }
}
