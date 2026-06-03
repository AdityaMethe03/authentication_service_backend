package com.authentication.auth_app_backend.modules.role;

import com.authentication.auth_app_backend.modules.role.dto.RoleDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tags(@Tag(name = "V1 Role"))
@AllArgsConstructor
@CrossOrigin
@RequestMapping("/api/v1/role")
public class RoleController {

  private final RoleService roleService;

  @PostMapping(value = "/register")
  public ResponseEntity<RoleDto> createRole(@RequestBody RoleDto role) {
    role.setId(UUID.randomUUID().toString());
    return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(role));
  }

  @PutMapping(value = "/update/{roleId}")
  public ResponseEntity<RoleDto> updateRole(
      @RequestBody RoleDto role, @PathVariable String roleId) {
    return ResponseEntity.ok(roleService.updateRole(role, roleId));
  }

  @DeleteMapping(value = "/delete/{roleId}")
  public void deleteRole(@PathVariable String roleId) {
    roleService.deleteRole(roleId);
  }

  @GetMapping(value = "/lookup/search/{roleId}")
  public ResponseEntity<RoleDto> getRoleById(@PathVariable String roleId) {
    return ResponseEntity.ok(roleService.getRoleById(roleId));
  }

  @GetMapping(value = "/lookup/search/all")
  public ResponseEntity<Iterable<RoleDto>> getAllRoles() {
    return ResponseEntity.ok(roleService.getAllRoles());
  }
}
