package com.authentication.auth_app_backend.security;

import com.authentication.auth_app_backend.common.exceptions.ResourceNotFoundException;

public final class TenantContext {
  public static final String TENANT_HEADER = "X-G-APP-Tenant-Id";
  public static final String SYSTEM_TENANT_ID = "SYSTEM";
  public static final String SUDO_ADMIN_EMAIL = "sudo@app.tech.in";

  private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();
  private static final ThreadLocal<String> AUTHENTICATED_TENANT_ID = new ThreadLocal<>();
  private static final ThreadLocal<Boolean> PLATFORM_ADMIN = new ThreadLocal<>();

  private TenantContext() {}

  public static String getTenantId() {
    String tenantId = TENANT_ID.get();
    if (tenantId == null || tenantId.isBlank()) {
      throw new ResourceNotFoundException("Tenant context is missing.");
    }
    return tenantId;
  }

  public static String getAuthenticatedTenantId() {
    return AUTHENTICATED_TENANT_ID.get();
  }

  public static boolean isPlatformAdmin() {
    return Boolean.TRUE.equals(PLATFORM_ADMIN.get());
  }

  static void setTenantId(String tenantId) {
    TENANT_ID.set(tenantId);
  }

  static void setAuthentication(String tenantId, boolean platformAdmin) {
    AUTHENTICATED_TENANT_ID.set(tenantId);
    PLATFORM_ADMIN.set(platformAdmin);
  }

  static void clear() {
    TENANT_ID.remove();
    AUTHENTICATED_TENANT_ID.remove();
    PLATFORM_ADMIN.remove();
  }
}
