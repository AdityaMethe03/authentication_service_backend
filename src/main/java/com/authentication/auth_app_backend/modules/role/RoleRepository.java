package com.authentication.auth_app_backend.modules.role;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoRepository<Role, String> {

  Optional<Role> findByName(String name);

  Optional<Role> findByIdentifier(String identifier);

  List<Role> findByIdentifierIn(Collection<String> identifiers);

  boolean existsByIdentifier(String identifier);
}
