package com.authentication.auth_app_backend;

import com.authentication.auth_app_backend.entities.Role;
import com.authentication.auth_app_backend.entities.enums.UserRole;
import com.authentication.auth_app_backend.repositories.RoleRepository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthAppBackendApplication implements CommandLineRunner {

  @Autowired private RoleRepository roleRepository;

  public static void main(String[] args) {
    SpringApplication.run(AuthAppBackendApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    roleRepository
        .findByName(UserRole.SUDO_ADMIN.name())
        .ifPresentOrElse(
            (role) -> {
              System.out.println(role.getName() + " role already exists.");
            },
            () -> {
              Role role = new Role();
              role.setId(UUID.randomUUID().toString());
              role.setName(UserRole.SUDO_ADMIN.name());
              roleRepository.save(role);
            });

    roleRepository
        .findByName(UserRole.ADMIN.name())
        .ifPresentOrElse(
            (role) -> {
              System.out.println(role.getName() + " role already exists.");
            },
            () -> {
              Role role = new Role();
              role.setId(UUID.randomUUID().toString());
              role.setName(UserRole.ADMIN.name());
              roleRepository.save(role);
            });

    roleRepository
        .findByName(UserRole.GUEST.name())
        .ifPresentOrElse(
            (role) -> {
              System.out.println(role.getName() + " role already exists.");
            },
            () -> {
              Role role = new Role();
              role.setId(UUID.randomUUID().toString());
              role.setName(UserRole.GUEST.name());
              roleRepository.save(role);
            });
  }
}
