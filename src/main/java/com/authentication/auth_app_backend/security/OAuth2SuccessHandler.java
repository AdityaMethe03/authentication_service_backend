package com.authentication.auth_app_backend.security;

import com.authentication.auth_app_backend.modules.auth.refreshtoken.RefreshToken;
import com.authentication.auth_app_backend.modules.auth.refreshtoken.RefreshTokenRepository;
import com.authentication.auth_app_backend.modules.role.enums.UserRole;
import com.authentication.auth_app_backend.modules.tenant.TenantRepository;
import com.authentication.auth_app_backend.modules.tenant.enums.TenantStatusEnum;
import com.authentication.auth_app_backend.modules.user.User;
import com.authentication.auth_app_backend.modules.user.UserRepository;
import com.authentication.auth_app_backend.modules.user.enums.Provider;
import com.authentication.auth_app_backend.modules.user.enums.UserStatusEnum;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final CookieService cookieService;
  private final RefreshTokenRepository refreshTokenRepository;
  private final TenantRepository tenantRepository;

  @Value("${app.auth.frontend.success-redirect}")
  private String frontEndSuccessUrl;

  @Override
  @Transactional
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {
    String tenantId = TenantContext.getTenantId();
    if (TenantContext.SYSTEM_TENANT_ID.equals(tenantId)
        || tenantRepository
            .findByTenantCodeIgnoreCase(tenantId)
            .map(t -> t.getStatus() != TenantStatusEnum.ACTIVE)
            .orElse(true)) {
      throw new IllegalArgumentException("OAuth sign-in is unavailable for the SYSTEM tenant.");
    }

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

    // identify user:
    // then username, user email, new user create
    String registrationId = "unknown";
    if (authentication instanceof OAuth2AuthenticationToken token) {
      registrationId = token.getAuthorizedClientRegistrationId();
    }

    logger.info("OAuth authentication completed for provider {}", registrationId);

    User user;
    switch (registrationId) {
      case "google" -> {
        String googleId = oAuth2User.getAttributes().getOrDefault("sub", "").toString();
        String email = oAuth2User.getAttributes().getOrDefault("email", "").toString();
        String normalizedEmail = email.trim().toLowerCase();
        rejectReservedSudoEmail(normalizedEmail);
        String name = oAuth2User.getAttributes().getOrDefault("name", "").toString();
        String picture = oAuth2User.getAttributes().getOrDefault("picture", "").toString();
        User newUser =
            User.builder()
                .email(normalizedEmail)
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .name(name)
                .image(picture)
                .enable(true)
                .provider(Provider.GOOGLE)
                .providerId(googleId)
                .roles(Set.of(UserRole.USER.name()))
                .status(UserStatusEnum.ACTIVE)
                .build();

        user =
            userRepository
                .findByTenantIdAndEmail(tenantId, normalizedEmail)
                .orElseGet(() -> userRepository.save(newUser));
      }

      case "github" -> {
        String name = oAuth2User.getAttributes().getOrDefault("login", "").toString();
        String githubId = oAuth2User.getAttributes().getOrDefault("id", "").toString();
        String picture = oAuth2User.getAttributes().getOrDefault("avatar_url", "").toString();

        String email = (String) oAuth2User.getAttributes().get("email");
        if (email == null) {
          email = name + "@github.com";
        }
        String normalizedEmail = email.trim().toLowerCase();
        rejectReservedSudoEmail(normalizedEmail);

        User newUser =
            User.builder()
                .email(normalizedEmail)
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .name(name)
                .image(picture)
                .enable(true)
                .provider(Provider.GITHUB)
                .providerId(githubId)
                .roles(Set.of(UserRole.USER.name()))
                .status(UserStatusEnum.ACTIVE)
                .build();

        user =
            userRepository
                .findByTenantIdAndEmail(tenantId, normalizedEmail)
                .orElseGet(() -> userRepository.save(newUser));
      }

      default -> {
        throw new RuntimeException("Invalid registrationId: " + registrationId);
      }
    }

    // jwt token - redirect to frontend with token

    //    user --> revoke all refresh tokens

    // will create refresh token
    String jti = UUID.randomUUID().toString();
    RefreshToken refreshTokenOb =
        RefreshToken.builder()
            .id(UUID.randomUUID().toString())
            .jti(jti)
            .tenantId(tenantId)
            .userId(user.getId())
            .revoked(false)
            .createdAt(new Date())
            .expiresAt(Date.from(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds())))
            .build();

    refreshTokenRepository.save(refreshTokenOb);
    String refreshToken = jwtService.generateRefreshToken(user, refreshTokenOb.getJti());

    cookieService.attachRefreshCookie(
        response, refreshToken, (int) jwtService.getRefreshTtlSeconds());

    //    response.getWriter().write("Login successful");
    response.sendRedirect(
        UriComponentsBuilder.fromUriString(frontEndSuccessUrl)
            .queryParam("tenantId", tenantId)
            .build()
            .encode()
            .toUriString());
  }

  private static void rejectReservedSudoEmail(String email) {
    if (TenantContext.SUDO_ADMIN_EMAIL.equalsIgnoreCase(email)) {
      throw new IllegalArgumentException("The reserved SUDO_ADMIN email cannot be used for OAuth.");
    }
  }
}
