package com.authentication.auth_app_backend;

import com.authentication.auth_app_backend.config.AppConstants;
import com.authentication.auth_app_backend.entities.Role;
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
        .findByName("ROLE_" + AppConstants.SUDO_ADMIN_ROLE)
        .ifPresentOrElse(
            (role) -> {
              System.out.println(role.getName() + " role already exists.");
            },
            () -> {
              Role role = new Role();
              role.setId(UUID.randomUUID().toString());
              role.setName("ROLE_" + AppConstants.SUDO_ADMIN_ROLE);
              roleRepository.save(role);
            });

    roleRepository
        .findByName("ROLE_" + AppConstants.ADMIN_ROLE)
        .ifPresentOrElse(
            (role) -> {
              System.out.println(role.getName() + " role already exists.");
            },
            () -> {
              Role role = new Role();
              role.setId(UUID.randomUUID().toString());
              role.setName("ROLE_" + AppConstants.ADMIN_ROLE);
              roleRepository.save(role);
            });

    roleRepository
        .findByName("ROLE_" + AppConstants.GUEST_ROLE)
        .ifPresentOrElse(
            (role) -> {
              System.out.println(role.getName() + " role already exists.");
            },
            () -> {
              Role role = new Role();
              role.setId(UUID.randomUUID().toString());
              role.setName("ROLE_" + AppConstants.GUEST_ROLE);
              roleRepository.save(role);
            });
  }
}
