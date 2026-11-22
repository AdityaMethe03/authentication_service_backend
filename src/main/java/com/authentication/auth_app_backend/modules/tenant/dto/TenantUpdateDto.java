package com.authentication.auth_app_backend.modules.tenant.dto;

import com.authentication.auth_app_backend.modules.tenant.enums.TenantStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantUpdateDto {
  @NotBlank private String name;
  @NotNull private TenantStatusEnum status;
}
