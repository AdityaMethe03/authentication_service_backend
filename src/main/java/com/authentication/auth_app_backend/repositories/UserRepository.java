package com.authentication.auth_app_backend.repositories;

import com.authentication.auth_app_backend.entities.User;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);
}
