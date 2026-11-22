package com.authentication.auth_app_backend.modules.role;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoRepository<Role, String> {

  Optional<Role> findByTenantIdAndName(String tenantId, String name);

  Optional<Role> findByTenantIdAndIdentifier(String tenantId, String identifier);

  Optional<Role> findByTenantIdAndId(String tenantId, String id);

  List<Role> findByTenantIdAndIdentifierIn(String tenantId, Collection<String> identifiers);

  boolean existsByTenantIdAndIdentifier(String tenantId, String identifier);

  List<Role> findAllByTenantId(String tenantId);
}
