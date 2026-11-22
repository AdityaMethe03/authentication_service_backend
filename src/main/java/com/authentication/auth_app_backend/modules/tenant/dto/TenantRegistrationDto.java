package com.authentication.auth_app_backend.modules.tenant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantRegistrationDto {
  @NotBlank private String tenantCode;
  @NotBlank private String name;
  @NotBlank private String contactPerson;

  @NotBlank @Email private String contactEmail;

  @NotBlank private String contactNumber;
}
