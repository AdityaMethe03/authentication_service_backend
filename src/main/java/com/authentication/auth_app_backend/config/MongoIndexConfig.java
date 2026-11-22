package com.authentication.auth_app_backend.config;

import com.authentication.auth_app_backend.modules.auth.refreshtoken.RefreshToken;
import com.authentication.auth_app_backend.modules.role.Role;
import com.authentication.auth_app_backend.modules.tenant.Tenant;
import com.authentication.auth_app_backend.modules.user.User;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoIndexConfig {

  private final MongoTemplate mongoTemplate;

  @EventListener(ApplicationReadyEvent.class)
  public void initIndexes() {
    mongoTemplate
        .indexOps(RefreshToken.class)
        .createIndex(
            new Index()
                .on("expiresAt", Sort.Direction.ASC)
                .expire(0, TimeUnit.SECONDS)); // 0 = delete at expiresAt
    mongoTemplate
        .indexOps(Role.class)
        .createIndex(
            new Index()
                .on("tenantId", Sort.Direction.ASC)
                .on("identifier", Sort.Direction.ASC)
                .unique());
    mongoTemplate
        .indexOps(User.class)
        .createIndex(
            new Index()
                .on("tenantId", Sort.Direction.ASC)
                .on("email", Sort.Direction.ASC)
                .unique());
    mongoTemplate
        .indexOps(Tenant.class)
        .createIndex(new Index().on("tenantCode", Sort.Direction.ASC).unique());
    mongoTemplate
        .indexOps(RefreshToken.class)
        .createIndex(
            new Index().on("tenantId", Sort.Direction.ASC).on("jti", Sort.Direction.ASC).unique());
  }
}
