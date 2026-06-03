package com.authentication.auth_app_backend.modules.role;

import com.authentication.auth_app_backend.modules.role.enums.RoleStatusEnum;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "roles")
public class Role {
  @Id private String id;
  private String name;
  private RoleStatusEnum status;
  private Date createdAt;
  private Date updatedAt;
}
