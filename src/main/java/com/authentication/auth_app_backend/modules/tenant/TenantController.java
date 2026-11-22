package com.authentication.auth_app_backend.modules.tenant;

import com.authentication.auth_app_backend.modules.tenant.dto.TenantRegistrationDto;
import com.authentication.auth_app_backend.modules.tenant.dto.TenantResponseDto;
import com.authentication.auth_app_backend.modules.tenant.dto.TenantUpdateDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Tags(@Tag(name = "V1 Tenant"))
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/api/v1/tenants")
public class TenantController {
  private final TenantService tenantService;

  @PostMapping("/register")
  @PreAuthorize("hasAuthority('TENANT:WRITE')")
  public ResponseEntity<TenantResponseDto> registerTenant(
      @Valid @RequestBody TenantRegistrationDto registration) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(tenantService.registerTenant(registration));
  }

  @GetMapping("/lookup/search/all")
  @PreAuthorize("hasAuthority('TENANT:READ')")
  public ResponseEntity<Iterable<TenantResponseDto>> getAllTenants() {
    return ResponseEntity.ok(tenantService.getAllTenants());
  }

  @PutMapping("/update/{tenantCode}")
  @PreAuthorize("hasAuthority('TENANT:UPDATE')")
  public ResponseEntity<TenantResponseDto> updateTenant(
      @PathVariable String tenantCode, @Valid @RequestBody TenantUpdateDto update) {
    return ResponseEntity.ok(tenantService.updateTenant(update, tenantCode));
  }
}
