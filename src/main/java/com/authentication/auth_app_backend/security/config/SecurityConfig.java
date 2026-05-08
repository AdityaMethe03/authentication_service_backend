package com.authentication.auth_app_backend.security.config;

import com.authentication.auth_app_backend.config.AppConstants;
import com.authentication.auth_app_backend.dtos.ApiError;
import com.authentication.auth_app_backend.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper)
      throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(AppConstants.AUTH_PUBLIC_URLS)
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .logout(AbstractHttpConfigurer::disable)
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(
                    (request, response, e) -> {
                      response.setStatus(401);
                      response.setContentType("application/json");
                      String message =
                          Optional.ofNullable((String) request.getAttribute("error"))
                              .orElse("Unauthorized Access! " + e.getMessage());
                      var apiError =
                          ApiError.of(
                              HttpStatus.UNAUTHORIZED.value(),
                              "Unauthorized Access!",
                              message,
                              request.getRequestURI());
                      response.getWriter().write(objectMapper.writeValueAsString(apiError));
                    }))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }
}
