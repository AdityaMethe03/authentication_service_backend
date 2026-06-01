package com.authentication.auth_app_backend.entities;

import com.authentication.auth_app_backend.entities.enums.Provider;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "users")
public class User implements UserDetails {
  @Id private String id;
  private String email;
  private String name;
  private String password;
  private String image;
  private boolean enable;
  private Date createdAt;
  private Date updatedAt;
  private Provider provider;
  private String providerId;
  private Set<String> roles;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    if (this.roles == null || this.roles.isEmpty()) {
      return java.util.List.of();
    }
    return roles.stream().map(SimpleGrantedAuthority::new).toList();
  }

  @Override
  public String getUsername() {
    return this.email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return this.enable;
  }
}
