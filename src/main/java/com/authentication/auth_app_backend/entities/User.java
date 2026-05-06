package com.authentication.auth_app_backend.entities;

import com.authentication.auth_app_backend.entities.enums.Provider;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "users")
public class User {
    @Id
    private UUID id;
    private String email;
    private String name;
    private String password;
    private String image;
    private String enable;
    private Date createdAt;
    private String updatedAt;
    private Provider provider;
    private String providerId;
    private Set<Role> roles;
}
