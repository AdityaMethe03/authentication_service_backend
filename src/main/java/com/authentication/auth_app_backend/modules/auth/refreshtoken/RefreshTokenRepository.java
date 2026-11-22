package com.authentication.auth_app_backend.modules.auth.refreshtoken;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
  Optional<RefreshToken> findByTenantIdAndJti(String tenantId, String jti);

  List<RefreshToken> findByTenantIdAndUserIdAndRevokedFalse(String tenantId, String userId);

  void deleteByTenantIdAndUserId(String tenantId, String userId);
}
