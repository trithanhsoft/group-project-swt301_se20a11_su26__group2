package com.swp391.coding_platform.controller.auth;

import com.nimbusds.jose.JOSEException;
import com.swp391.coding_platform.dto.request.AuthenticationRequest;
import com.swp391.coding_platform.dto.request.ChangePasswordRequest;
import com.swp391.coding_platform.dto.request.GoogleLoginRequest;
import com.swp391.coding_platform.dto.request.RegisterRequest;
import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.dto.response.AuthenticationResponse;
import com.swp391.coding_platform.service.auth.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService authenticationService;

    // ACCESS TOKEN
    @NonFinal
    @Value("${auth.cookie.access-token.name}")
    String accessTokenName;

    @NonFinal
    @Value("${auth.cookie.access-token.secure}")
    boolean isCookieAccessTokenSecure;

    @NonFinal
    @Value("${auth.cookie.access-token.max-age}")
    long accessTokenMaxAge;

    @NonFinal
    @Value("${auth.cookie.access-token.http-only}")
    boolean accessTokenHttpOnly;

    @NonFinal
    @Value("${auth.cookie.access-token.same-site}")
    String accessTokenSameSite;

    @NonFinal
    @Value("${auth.cookie.access-token.path}")
    String accessTokenPath;

    // REFRESH TOKEN
    @NonFinal
    @Value("${auth.cookie.refresh-token.name}")
    String refreshTokenName;

    @NonFinal
    @Value("${auth.cookie.refresh-token.secure}")
    boolean isCookieRefreshTokenSecure;

    @NonFinal
    @Value("${auth.cookie.refresh-token.max-age}")
    long refreshTokenMaxAge;

    @NonFinal
    @Value("${auth.cookie.refresh-token.http-only}")
    boolean refreshTokenHttpOnly;

    @NonFinal
    @Value("${auth.cookie.refresh-token.same-site}")
    String refreshTokenSameSite;

    @NonFinal
    @Value("${auth.cookie.refresh-token.path}")
    String refreshTokenPath;


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @RequestBody AuthenticationRequest authenticationRequest, HttpServletResponse response){

        AuthenticationResponse result = authenticationService.login(authenticationRequest);
        addAuthCookies(response, result);

        return ResponseEntity.ok(ApiResponse.<AuthenticationResponse>builder()
                .status(200)
                .code(1000)
                .message("Login successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> googleLogin(
            @RequestBody @Valid GoogleLoginRequest googleLoginRequest, HttpServletResponse response){

        AuthenticationResponse result = authenticationService.googleLogin(googleLoginRequest);
        addAuthCookies(response, result);

        return ResponseEntity.ok(ApiResponse.<AuthenticationResponse>builder()
                .status(200)
                .code(1000)
                .message("Google login successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> register(
            @RequestBody @Valid RegisterRequest registerRequest, HttpServletResponse response){

        AuthenticationResponse result = authenticationService.register(registerRequest);
        addAuthCookies(response, result);

        return ResponseEntity.ok(ApiResponse.<AuthenticationResponse>builder()
                .status(200)
                .code(1000)
                .message("Register account successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = "access_token", required = false) String accessToken,
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response){

        authenticationService.logout(accessToken, refreshToken);
        clearAuthCookies(response);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .code(1000)
                .message("Logout successfully")
                .result(null)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken
            , HttpServletRequest request, HttpServletResponse response)
            throws ParseException, JOSEException {

        var result = authenticationService.refresh(refreshToken);
        addAuthCookies(response, result);

        return ResponseEntity.ok(ApiResponse.<AuthenticationResponse>builder()
                .status(200)
                .code(1000)
                .message("Refresh token successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    // ==================== Private Helper Methods ====================

    /**
     * Sets access token and refresh token as HttpOnly cookies on the response,
     * then clears the token values from the response body for security.
     */
    private void addAuthCookies(HttpServletResponse response, AuthenticationResponse result) {
        ResponseCookie accessTokenCookie = buildCookie(
                accessTokenName, result.getAccessToken(),
                accessTokenHttpOnly, isCookieAccessTokenSecure,
                accessTokenPath, accessTokenMaxAge, accessTokenSameSite);

        ResponseCookie refreshTokenCookie = buildCookie(
                refreshTokenName, result.getRefreshToken(),
                refreshTokenHttpOnly, isCookieRefreshTokenSecure,
                refreshTokenPath, refreshTokenMaxAge, refreshTokenSameSite);

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        // Clear tokens from response body — they are now in cookies only
        result.setAccessToken(null);
        result.setRefreshToken(null);
    }

    /**
     * Clears access token and refresh token cookies by setting maxAge to 0.
     */
    private void clearAuthCookies(HttpServletResponse response) {
        ResponseCookie expiredAccessToken = buildCookie(
                accessTokenName, "",
                accessTokenHttpOnly, isCookieAccessTokenSecure,
                accessTokenPath, 0, accessTokenSameSite);

        ResponseCookie expiredRefreshToken = buildCookie(
                refreshTokenName, "",
                refreshTokenHttpOnly, isCookieRefreshTokenSecure,
                refreshTokenPath, 0, refreshTokenSameSite);

        response.addHeader(HttpHeaders.SET_COOKIE, expiredAccessToken.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, expiredRefreshToken.toString());
    }

    private ResponseCookie buildCookie(String name, String value,
                                       boolean httpOnly, boolean secure,
                                       String path, long maxAge, String sameSite) {
        return ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(secure)
                .path(path)
                .maxAge(maxAge)
                .sameSite(sameSite)
                .build();
    }

}
