package com.authentication.auth_app_backend.modules.tenant;

import com.authentication.auth_app_backend.modules.tenant.enums.TenantStatusEnum;
import java.util.Date;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "tenants")
public class Tenant {
  @Id private String id;
  private String tenantCode;
  private String name;
  private String contactPerson;
  private String contactEmail;
  private String contactNumber;
  private TenantStatusEnum status;
  private Date createdAt;
}
