package com.authentication.auth_app_backend.modules.role.enums;

import java.util.Arrays;

public enum UserRole {
  SUDO_ADMIN,
  ADMIN,
  GUEST;

  public static String[] ALl() {
    return Arrays.stream(values()).map(Enum::name).toArray(String[]::new);
  }
}
