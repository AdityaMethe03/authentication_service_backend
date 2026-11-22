package com.authentication.auth_app_backend.modules.tenant.dto;

import com.authentication.auth_app_backend.modules.tenant.enums.TenantStatusEnum;
import java.util.Date;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantResponseDto {
  private String id;
  private String tenantCode;
  private String name;
  private String contactPerson;
  private String contactEmail;
  private String contactNumber;
  private TenantStatusEnum status;
  private Date createdAt;
}
