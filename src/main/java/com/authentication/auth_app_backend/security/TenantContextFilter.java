package com.authentication.auth_app_backend.security;

import com.authentication.auth_app_backend.modules.tenant.Tenant;
import com.authentication.auth_app_backend.modules.tenant.TenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class TenantContextFilter extends OncePerRequestFilter {
  private static final String OAUTH_TENANT_SESSION_KEY = "oauth2.selectedTenantId";
  private final TenantRepository tenantRepository;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String path = request.getServletPath();
      String tenantSelector = request.getHeader(TenantContext.TENANT_HEADER);
      if ((tenantSelector == null || tenantSelector.isBlank())
          && path.startsWith("/oauth2/authorization/")) {
        tenantSelector = request.getParameter("tenantId");
      }
      if ((tenantSelector == null || tenantSelector.isBlank())
          && path.startsWith("/login/oauth2/code/")) {
        var session = request.getSession(false);
        if (session != null) {
          tenantSelector = (String) session.getAttribute(OAUTH_TENANT_SESSION_KEY);
          session.removeAttribute(OAUTH_TENANT_SESSION_KEY);
        }
      }
      if (tenantSelector == null || tenantSelector.isBlank()) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant header is required.");
        return;
      }

      String selectedTenant = tenantSelector.trim();
      Tenant tenant = tenantRepository.findByTenantCodeIgnoreCase(selectedTenant).orElse(null);
      if (tenant == null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Tenant was not found.");
        return;
      }

      if (path.startsWith("/oauth2/authorization/")) {
        request.getSession(true).setAttribute(OAUTH_TENANT_SESSION_KEY, tenant.getTenantCode());
      }
      TenantContext.setTenantId(tenant.getTenantCode());
      filterChain.doFilter(request, response);
    } finally {
      TenantContext.clear();
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    return "OPTIONS".equalsIgnoreCase(request.getMethod())
        || path.startsWith("/swagger-ui")
        || path.startsWith("/api-docs")
        || path.startsWith("/v3/api-docs");
  }
}
