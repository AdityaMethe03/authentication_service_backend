package com.authentication.auth_app_backend.security.config;

import com.authentication.auth_app_backend.config.AppConstants;
import com.authentication.auth_app_backend.dtos.ApiError;
import com.authentication.auth_app_backend.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.lang.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(AppConstants.AUTH_PUBLIC_URLS)
                    .permitAll()
                    .requestMatchers(HttpMethod.GET)
                    .hasRole(AppConstants.GUEST_ROLE)
                    .requestMatchers("/api/v1/users/**")
                    .hasRole(AppConstants.ADMIN_ROLE)
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
                          var objectMapper = new ObjectMapper();
                          response.getWriter().write(objectMapper.writeValueAsString(apiError));
                        })
                    .accessDeniedHandler(
                        ((request, response, e) -> {
                          response.setStatus(403);
                          response.setContentType("application/json");
                          String message = e.getMessage();
                          String error = (String) request.getAttribute("error");
                          if (error != null) {
                            message = error;
                          }
                          var apiError =
                              ApiError.of(
                                  HttpStatus.FORBIDDEN.value(),
                                  "Forbidden Access!",
                                  message,
                                  request.getRequestURI(),
                                  true);
                          var objectMapper = new ObjectMapper();
                          response.getWriter().write(objectMapper.writeValueAsString(apiError));
                        })))
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

  @Bean
  public CorsConfigurationSource corsConfigurationSource(
      @Value("${app.cors.front-end-url}") String corsUrls) {
    String[] urls = corsUrls.trim().split(",");
    var config = new CorsConfiguration();
    config.setAllowedOrigins(Arrays.asList(urls));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
