package com.authentication.auth_app_backend.repositories;

import com.authentication.auth_app_backend.entities.Role;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoRepository<Role, String> {

  Optional<Role> findByName(String name);
}
