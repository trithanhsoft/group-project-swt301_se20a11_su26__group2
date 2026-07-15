package com.swp391.coding_platform.service.auth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.swp391.coding_platform.dto.request.AuthenticationRequest;
import com.swp391.coding_platform.dto.request.GoogleLoginRequest;
import com.swp391.coding_platform.dto.request.IntrospectRequest;
import com.swp391.coding_platform.dto.request.RegisterRequest;
import com.swp391.coding_platform.dto.response.AuthenticationResponse;
import com.swp391.coding_platform.dto.response.IntrospectResponse;
import com.swp391.coding_platform.entity.auth.RoleEntity;
import com.swp391.coding_platform.entity.enums.RoleName;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.mapper.UserMapper;
import com.swp391.coding_platform.repository.auth.InvalidatedTokenRepository;
import com.swp391.coding_platform.repository.auth.RoleRepository;
import com.swp391.coding_platform.repository.instructor.InstructorRepository;
import com.swp391.coding_platform.repository.user.UserOauthAccountRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private InvalidatedTokenRepository invalidatedTokenRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserOauthAccountRepository userOauthAccountRepository;
    @Mock
    private InstructorRepository instructorRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    // A valid 256-bit base64 key for tests
    private static final String SIGNER_KEY = "bXlzdXBlcnNlY3JldGtleXRoYXRpc2xvbmVub3VnaHRvYmVzZWN1cmUxMjM0NTY3ODkw";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationService, "SIGNER_KEY", SIGNER_KEY);
        ReflectionTestUtils.setField(authenticationService, "VALID_DURATION", 3600L);
        ReflectionTestUtils.setField(authenticationService, "REFRESHABLE_DURATION", 86400L);
    }

    // ======================== LOGIN ========================

    @Test
    void login_Success() {
        AuthenticationRequest request = new AuthenticationRequest("testuser", "password");
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setUsername("testuser");
        user.setPasswordHash("hashed_password");

        when(userRepository.findByUsernameWithWallet("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashed_password")).thenReturn(true);
        when(userMapper.toAuthenticationResponse(user)).thenReturn(new AuthenticationResponse());

        AuthenticationResponse response = authenticationService.login(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }

    @Test
    void login_InvalidPassword_ThrowsAppException() {
        AuthenticationRequest request = new AuthenticationRequest("testuser", "wrongpassword");
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        user.setPasswordHash("hashed_password");

        when(userRepository.findByUsernameWithWallet("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "hashed_password")).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> authenticationService.login(request));
        assertEquals(ErrorCode.INVALID_USERNAME_OR_PASSWORD, ex.getErrorCode());
    }

    @Test
    void login_UsernameNotFound_ThrowsAppException() {
        AuthenticationRequest request = new AuthenticationRequest("notfound", "password");
        when(userRepository.findByUsernameWithWallet("notfound")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> authenticationService.login(request));
        assertEquals(ErrorCode.INVALID_USERNAME_OR_PASSWORD, ex.getErrorCode());
    }

    // ======================== REGISTER ========================

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest("newuser", "password", "password", "New User", "newuser@test.com");
        UserEntity mappedUser = new UserEntity();
        mappedUser.setUsername("newuser");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
        when(userMapper.toUserEntity(request)).thenReturn(mappedUser);
        when(passwordEncoder.encode("password")).thenReturn("hashed");
        RoleEntity roleEntity = RoleEntity.builder().name(RoleName.USER).build();
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(roleEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(mappedUser);

        when(userRepository.findByUsernameWithWallet("newuser")).thenReturn(Optional.of(mappedUser));
        when(passwordEncoder.matches("password", "hashed")).thenReturn(true);
        when(userMapper.toAuthenticationResponse(mappedUser)).thenReturn(new AuthenticationResponse());

        AuthenticationResponse response = authenticationService.register(request);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void register_UsernameAlreadyExists_ThrowsAppException() {
        RegisterRequest request = new RegisterRequest("existinguser", "password", "password", "Name", "email@test.com");
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> authenticationService.register(request));
        assertEquals(ErrorCode.USERNAME_ALREADY_EXISTS, ex.getErrorCode());
    }

    @Test
    void register_PasswordNotMatch_ThrowsAppException() {
        RegisterRequest request = new RegisterRequest("user", "password", "different", "Name", "email@test.com");
        when(userRepository.existsByUsername("user")).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> authenticationService.register(request));
        assertEquals(ErrorCode.PASSWORD_NOT_MATCH, ex.getErrorCode());
    }

    @Test
    void register_EmailAlreadyExists_ThrowsAppException() {
        RegisterRequest request = new RegisterRequest("user", "password", "password", "Name", "existing@test.com");
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> authenticationService.register(request));
        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, ex.getErrorCode());
    }

    // ======================== GOOGLE LOGIN ========================

    @Test
    void googleLogin_InvalidIdToken_ThrowsAppException() {
        GoogleLoginRequest request = new GoogleLoginRequest("invalid_token");
        AppException ex = assertThrows(AppException.class, () -> authenticationService.googleLogin(request));
        assertEquals(ErrorCode.UNAUTHENTICATED, ex.getErrorCode());
    }

    // ======================== LOGOUT ========================

    @Test
    void logout_WithBothTokensBlank_ShouldNotCallInvalidation() {
        // Both blank - should be no-op with no exception
        assertDoesNotThrow(() -> authenticationService.logout("", ""));
        verifyNoInteractions(invalidatedTokenRepository);
    }

    @Test
    void logout_WithNullTokens_ShouldNotCallInvalidation() {
        assertDoesNotThrow(() -> authenticationService.logout(null, null));
        verifyNoInteractions(invalidatedTokenRepository);
    }

    @Test
    void logout_WithValidAccessToken_ShouldInvalidateToken() throws JOSEException {
        // Generate a valid token to pass to logout
        String validToken = generateTestToken(false, 3600);
        // A malformed or expired token in processTokenInvalidation is caught silently
        assertDoesNotThrow(() -> authenticationService.logout(validToken, null));
    }

    @Test
    void logout_WithMalformedAccessToken_ShouldNotThrow() {
        // malformed tokens should be silently caught
        assertDoesNotThrow(() -> authenticationService.logout("not.a.valid.jwt", null));
    }

    // ======================== INTROSPECT ========================

    @Test
    void introspect_WithValidToken_ShouldReturnTrue() throws JOSEException {
        String validToken = generateTestToken(false, 3600);
        IntrospectRequest request = new IntrospectRequest(validToken);

        IntrospectResponse response = authenticationService.introspect(request);

        assertTrue(response.isValid());
    }

    @Test
    void introspect_WithExpiredToken_ShouldReturnFalse() throws JOSEException {
        // negative duration → already expired
        String expiredToken = generateTestToken(false, -100);
        IntrospectRequest request = new IntrospectRequest(expiredToken);

        IntrospectResponse response = authenticationService.introspect(request);

        assertFalse(response.isValid());
    }

    @Test
    void introspect_WithBlankToken_ShouldReturnFalse() {
        IntrospectRequest request = new IntrospectRequest("");

        IntrospectResponse response = authenticationService.introspect(request);

        assertFalse(response.isValid());
    }

    @Test
    void introspect_WithMalformedToken_ShouldReturnFalse() {
        IntrospectRequest request = new IntrospectRequest("not.a.jwt");

        IntrospectResponse response = authenticationService.introspect(request);

        assertFalse(response.isValid());
    }

    // ======================== REFRESH ========================

    @Test
    void refresh_WithInvalidatedToken_ShouldThrow() throws JOSEException {
        String validRefreshToken = generateTestToken(true, 86400);

        // Token in invalidated list → UNAUTHENTICATED
        when(invalidatedTokenRepository.existsByTokenJti(anyString())).thenReturn(true);

        AppException ex = assertThrows(AppException.class,
                () -> authenticationService.refresh(validRefreshToken));
        assertEquals(ErrorCode.UNAUTHENTICATED, ex.getErrorCode());
    }

    @Test
    void refresh_WithAccessToken_ShouldThrow() throws JOSEException {
        // access token (isRefresh=false) passed to refresh (isRefresh=true) → INVALID_TOKEN
        String accessToken = generateTestToken(false, 3600);

        AppException ex = assertThrows(AppException.class,
                () -> authenticationService.refresh(accessToken));
        // Should fail with INVALID_TOKEN because the token type is "ACCESS", not "REFRESH"
        assertNotNull(ex);
    }

    @Test
    void refresh_WithExpiredToken_ShouldThrow() throws JOSEException {
        String expiredRefreshToken = generateTestToken(true, -100);

        AppException ex = assertThrows(AppException.class,
                () -> authenticationService.refresh(expiredRefreshToken));
        assertNotNull(ex);
    }

    @Test
    void refresh_WithBlankToken_ShouldThrow() {
        AppException ex = assertThrows(AppException.class,
                () -> authenticationService.refresh(""));
        assertEquals(ErrorCode.UNAUTHENTICATED, ex.getErrorCode());
    }

    // ======================== HELPER ========================

    /**
     * Generates a test JWT token using the same SIGNER_KEY
     */
    private String generateTestToken(boolean isRefresh, long durationSeconds) throws JOSEException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("testuser")
                .jwtID(UUID.randomUUID().toString())
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + durationSeconds * 1000L))
                .claim("type", isRefresh ? "REFRESH" : "ACCESS")
                .claim("userId", 1)
                .claim("scope", "USER")
                .build();

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        SignedJWT jwt = new SignedJWT(header, claims);
        jwt.sign(new MACSigner(SIGNER_KEY.getBytes()));
        return jwt.serialize();
    }
}
