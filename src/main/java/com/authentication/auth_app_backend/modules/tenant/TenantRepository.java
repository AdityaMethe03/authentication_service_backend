package com.authentication.auth_app_backend.modules.tenant;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TenantRepository extends MongoRepository<Tenant, String> {
  Optional<Tenant> findByTenantCodeIgnoreCase(String tenantCode);

  boolean existsByTenantCodeIgnoreCase(String tenantCode);

  List<Tenant> findAllByOrderByNameAsc();
}
