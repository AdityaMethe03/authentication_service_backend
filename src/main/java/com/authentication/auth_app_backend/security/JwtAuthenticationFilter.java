package com.authentication.auth_app_backend.security;

import com.authentication.auth_app_backend.modules.role.enums.UserRole;
import com.authentication.auth_app_backend.modules.tenant.TenantRepository;
import com.authentication.auth_app_backend.modules.tenant.enums.TenantStatusEnum;
import com.authentication.auth_app_backend.modules.user.UserRepository;
import io.jsonwebtoken.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserRepository userRepository;
  private final TenantRepository tenantRepository;
  private final AuthorityResolver authorityResolver;
  private final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String header = request.getHeader("Authorization");

    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      try {
        if (!jwtService.isAccessToken(token)) {
          filterChain.doFilter(request, response);
          return;
        }

        Claims payload = jwtService.parse(token).getPayload();
        String userId = payload.getSubject();
        String authenticatedTenantId = jwtService.getTenantId(token);
        var user =
            userRepository
                .findByTenantIdAndId(authenticatedTenantId, userId)
                .orElseThrow(() -> new JwtException("User is not valid in this tenant."));
        boolean isPlatformAdmin =
            TenantContext.SYSTEM_TENANT_ID.equals(authenticatedTenantId)
                && user.getRoles() != null
                && user.getRoles().contains(UserRole.SUDO_ADMIN.name());
        var requestedTenant =
            tenantRepository
                .findByTenantCodeIgnoreCase(TenantContext.getTenantId())
                .orElseThrow(() -> new JwtException("Request tenant does not exist."));
        if (requestedTenant.getStatus() != TenantStatusEnum.ACTIVE && !isPlatformAdmin) {
          throw new JwtException("Tenant is inactive.");
        }
        if (!authenticatedTenantId.equals(TenantContext.getTenantId()) && !isPlatformAdmin) {
          throw new JwtException("Token tenant does not match request tenant.");
        }
        if (!user.isEnable()) {
          throw new JwtException("User is disabled.");
        }

        List<GrantedAuthority> authorities =
            authorityResolver.resolve(authenticatedTenantId, user.getRoles());
        TenantContext.setAuthentication(authenticatedTenantId, isPlatformAdmin);
        var auth = new UsernamePasswordAuthenticationToken(user.getId(), null, authorities);
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
          SecurityContextHolder.getContext().setAuthentication(auth);
        }
      } catch (ExpiredJwtException e) {
        request.setAttribute("error", "Token expired");
      } catch (Exception e) {
        request.setAttribute("error", "Invalid Token");
      }
    }
    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return request.getServletPath().startsWith("/api/v1/auth/");
  }
}
