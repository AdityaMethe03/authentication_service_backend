package com.authentication.auth_app_backend.modules.tenant;

import com.authentication.auth_app_backend.modules.tenant.dto.TenantRegistrationDto;
import com.authentication.auth_app_backend.modules.tenant.dto.TenantResponseDto;
import com.authentication.auth_app_backend.modules.tenant.dto.TenantUpdateDto;

public interface TenantService {
  TenantResponseDto registerTenant(TenantRegistrationDto registration);

  TenantResponseDto updateTenant(TenantUpdateDto update, String tenantCode);

  Iterable<TenantResponseDto> getAllTenants();
}
