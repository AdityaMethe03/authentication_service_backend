package com.authentication.auth_app_backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info =
        @Info(
            title = "Auth Application build by Aditya Methe",
            description = "Generic auth app that can be used with any application.",
            contact = @Contact(name = "Aditya Methe", url = "", email = "metheaditya9@gmail.com"),
            version = "1.0.0",
            summary =
                "This is a common authentication application which can be used in any application."))
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer", // Authorization: Bearer token
    bearerFormat = "JWT")
@SecurityScheme(
    name = "tenantHeader",
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.HEADER,
    paramName = "X-G-APP-Tenant-Id",
    description = "Tenant code, for example SYSTEM")
public class APIDocConfig {
  @Bean
  public OpenApiCustomizer tenantAndBearerSecurity() {
    return openApi ->
        openApi.addSecurityItem(
            new SecurityRequirement().addList("bearerAuth").addList("tenantHeader"));
  }
}
