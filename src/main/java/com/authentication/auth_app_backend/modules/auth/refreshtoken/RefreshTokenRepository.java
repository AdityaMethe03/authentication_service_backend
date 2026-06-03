package com.authentication.auth_app_backend.modules.auth.refreshtoken;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
  Optional<RefreshToken> findByJti(String jti);
}
