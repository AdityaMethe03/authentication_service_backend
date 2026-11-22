package com.authentication.auth_app_backend.security.config;

import com.authentication.auth_app_backend.common.dtos.ApiErrorDto;
import com.authentication.auth_app_backend.config.SecurityUrls;
import com.authentication.auth_app_backend.security.JwtAuthenticationFilter;
import com.authentication.auth_app_backend.security.OAuth2SuccessHandler;
import com.authentication.auth_app_backend.security.TenantContextFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.lang.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final TenantContextFilter tenantContextFilter;
  private final OAuth2SuccessHandler oAuth2SuccessHandler;
  private final ObjectMapper objectMapper;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .oauth2Login(oauth -> oauth.successHandler(oAuth2SuccessHandler))
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(SecurityUrls.AUTH_PUBLIC_URLS)
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .logout(AbstractHttpConfigurer::disable)
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(
                        (request, response, e) -> {
                          var apiError =
                              ApiErrorDto.of(
                                  HttpStatus.UNAUTHORIZED,
                                  "SESSION_UNAUTHORIZED",
                                  "Your session is invalid or has expired. Please sign in again.",
                                  request.getRequestURI());
                          response.setStatus(apiError.status());
                          response.setContentType("application/problem+json");
                          response.setHeader("X-Request-Id", apiError.requestId());
                          response.getWriter().write(objectMapper.writeValueAsString(apiError));
                        })
                    .accessDeniedHandler(
                        ((request, response, e) -> {
                          var apiError =
                              ApiErrorDto.of(
                                  HttpStatus.FORBIDDEN,
                                  "ACCESS_DENIED",
                                  "You don't have permission to perform this action.",
                                  request.getRequestURI());
                          response.setStatus(apiError.status());
                          response.setContentType("application/problem+json");
                          response.setHeader("X-Request-Id", apiError.requestId());
                          response.getWriter().write(objectMapper.writeValueAsString(apiError));
                        })))
        .addFilterBefore(tenantContextFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(jwtAuthenticationFilter, TenantContextFilter.class);

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
