package com.authentication.auth_app_backend.modules.auth;

import com.authentication.auth_app_backend.modules.auth.dto.LoginRequest;
import com.authentication.auth_app_backend.modules.auth.dto.RefreshTokenRequest;
import com.authentication.auth_app_backend.modules.auth.dto.TokenResponse;
import com.authentication.auth_app_backend.modules.auth.refreshtoken.RefreshToken;
import com.authentication.auth_app_backend.modules.auth.refreshtoken.RefreshTokenRepository;
import com.authentication.auth_app_backend.modules.user.User;
import com.authentication.auth_app_backend.modules.user.UserRepository;
import com.authentication.auth_app_backend.modules.user.dto.UserDto;
import com.authentication.auth_app_backend.security.CookieService;
import com.authentication.auth_app_backend.security.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;
  private final RefreshTokenRepository refreshTokenRepository;

  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final ModelMapper modelMapper;
  private final CookieService cookieService;

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(
      @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
    // authenticate
    Authentication authenticate = authenticate(loginRequest);
    User user =
        userRepository
            .findByEmail(loginRequest.email())
            .orElseThrow(() -> new BadCredentialsException("Invalid Username or Password."));
    if (!user.isEnable()) {
      throw new DisabledException("User is disabled");
    }

    String jti = UUID.randomUUID().toString();

    var refreshTokenOb =
        RefreshToken.builder() // refresh token object
            .jti(jti)
            .userId(user.getId())
            .createdAt(new Date())
            .expiresAt(Date.from(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds())))
            .revoked(false)
            .build();

    // save refresh token INFORMATION
    refreshTokenRepository.save(refreshTokenOb);

    // generate token
    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateRefreshToken(user, refreshTokenOb.getJti());

    // use cookie service to attach a refresh token in a cookie
    cookieService.attachRefreshCookie(
        response, refreshToken, (int) jwtService.getRefreshTtlSeconds());
    cookieService.addNoStoreHeaders(response);

    TokenResponse tokenResponse =
        TokenResponse.of(
            accessToken,
            refreshToken,
            jwtService.getAccessTtlSeconds(),
            modelMapper.map(user, UserDto.class));
    return ResponseEntity.ok(tokenResponse);
  }

  private Authentication authenticate(LoginRequest loginRequest) {
    try {
      return authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
    } catch (Exception e) {
      e.printStackTrace();
      throw new BadCredentialsException("Invalid Username or Password.");
    }
  }

  // Api to renew access and refresh token
  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refreshToken(
      @RequestBody(required = false) RefreshTokenRequest body,
      HttpServletRequest request,
      HttpServletResponse response) {

    // This method will read the refresh token from the request header or body
    String refreshToken =
        readRefreshTokenFromRequest(body, request)
            .orElseThrow(() -> new BadCredentialsException("Refresh token is missing."));

    if (!jwtService.isRefreshToken(refreshToken)) {
      throw new BadCredentialsException("Invalid Refresh Token Type.");
    }

    String jti = jwtService.getJti(refreshToken);
    UUID userId = jwtService.getUserId(refreshToken);
    RefreshToken storedRefreshToken =
        refreshTokenRepository
            .findByJti(jti)
            .orElseThrow(() -> new BadCredentialsException("Refresh Token not recognized."));

    User storedUser =
        userRepository
            .findById(storedRefreshToken.getUserId())
            .orElseThrow(() -> new BadCredentialsException("User not found."));

    if (storedRefreshToken.isRevoked()) {
      throw new BadCredentialsException("Refresh Token Expired or Revoked");
    }

    if (storedRefreshToken.getExpiresAt().before(new Date())) {
      throw new BadCredentialsException("Refresh Token Expired.");
    }

    // Rotate refresh token
    storedRefreshToken.setRevoked(true);
    String newJti = UUID.randomUUID().toString();
    storedRefreshToken.setReplacedByToken(newJti);
    refreshTokenRepository.save(storedRefreshToken);

    var newRefreshTokenOb =
        RefreshToken.builder()
            .jti(newJti)
            .userId(storedUser.getId())
            .createdAt(new Date())
            .expiresAt(Date.from(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds())))
            .revoked(false)
            .build();

    refreshTokenRepository.save(newRefreshTokenOb);
    String newAccessToken = jwtService.generateAccessToken(storedUser);
    String newRefreshToken =
        jwtService.generateRefreshToken(storedUser, newRefreshTokenOb.getJti());

    cookieService.attachRefreshCookie(
        response, newRefreshToken, (int) jwtService.getRefreshTtlSeconds());
    cookieService.addNoStoreHeaders(response);
    return ResponseEntity.ok(
        TokenResponse.of(
            newAccessToken,
            newRefreshToken,
            jwtService.getAccessTtlSeconds(),
            modelMapper.map(storedUser, UserDto.class)));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
    readRefreshTokenFromRequest(null, request)
        .ifPresent(
            token -> {
              try {
                if (jwtService.isRefreshToken(token)) {
                  String jti = jwtService.getJti(token);
                  refreshTokenRepository
                      .findByJti(jti)
                      .ifPresent(
                          rt -> {
                            rt.setRevoked(true);
                            refreshTokenRepository.save(rt);
                          });
                }
              } catch (JwtException ignored) {
              }
            });

    // Use CookieUtil (same behavior)
    cookieService.clearRefreshCookie(response);
    cookieService.addNoStoreHeaders(response);
    SecurityContextHolder.clearContext();
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  private Optional<String> readRefreshTokenFromRequest(
      RefreshTokenRequest body, HttpServletRequest request) {
    // 1. prefer reading the refresh token from a cookie
    if (request.getCookies() != null) {
      Optional<String> fromCookie =
          Arrays.stream(request.getCookies())
              .filter((Cookie c) -> cookieService.getRefreshTokenCookieName().equals(c.getName()))
              .map(Cookie::getValue)
              .filter((String v) -> !v.isBlank())
              .findFirst();

      if (fromCookie.isPresent()) {
        return fromCookie;
      }
    }
    // 2. body
    if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
      return Optional.of(body.refreshToken());
    }

    // 3. custom header
    String refreshHeader = request.getHeader("X-Refresh-Token");
    if (refreshHeader != null && !refreshHeader.isBlank()) {
      return Optional.of(refreshHeader.trim());
    }

    // 4. Authorization = Bearer <token> (token is Refresh Token)
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer", 0, 7)) {
      String candidate = authHeader.substring(7).trim();
      if (!candidate.isEmpty()) {
        try {
          if (jwtService.isRefreshToken(candidate)) {
            return Optional.of(candidate);
          }
        } catch (Exception ignored) {

        }
      }
    }

    return Optional.empty();
  }

  @PostMapping("/register")
  public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));
  }
}
