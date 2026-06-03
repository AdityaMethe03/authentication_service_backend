package com.authentication.auth_app_backend.entities.enums;

import java.util.Arrays;

public enum UserRole {
  SUDO_ADMIN,
  ADMIN,
  GUEST;

  public static String[] ALl() {
    return Arrays.stream(values()).map(Enum::name).toArray(String[]::new);
  }
}
