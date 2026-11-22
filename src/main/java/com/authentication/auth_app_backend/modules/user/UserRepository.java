package com.authentication.auth_app_backend.modules.user;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

  Optional<User> findByTenantIdAndEmail(String tenantId, String email);

  Optional<User> findByTenantIdAndId(String tenantId, String id);

  boolean existsByTenantIdAndEmail(String tenantId, String email);

  List<User> findAllByTenantId(String tenantId);

  List<User> findByTenantIdAndRolesContaining(String tenantId, String role);

  Optional<User> findByTenantIdAndProviderAndProviderId(
      String tenantId,
      com.authentication.auth_app_backend.modules.user.enums.Provider provider,
      String providerId);
}
