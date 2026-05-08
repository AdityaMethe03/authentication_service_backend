package com.authentication.auth_app_backend.repositories;

import com.authentication.auth_app_backend.entities.RefreshToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, UUID> {
  Optional<RefreshToken> findByJti(String jti);
}
