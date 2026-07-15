package com.swp391.coding_platform.security;

import com.swp391.coding_platform.dto.request.IntrospectRequest;
import com.swp391.coding_platform.dto.response.IntrospectResponse;
import com.swp391.coding_platform.service.auth.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomJwtDecoderTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private NimbusJwtDecoder nimbusJwtDecoder;

    @InjectMocks
    private CustomJwtDecoder customJwtDecoder;

    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String INVALID_TOKEN = "invalid.jwt.token";

    @BeforeEach
    void setUp() throws Exception {
        java.lang.reflect.Field field = CustomJwtDecoder.class.getDeclaredField("nimbusJwtDecoder");
        field.setAccessible(true);
        field.set(customJwtDecoder, nimbusJwtDecoder);
    }

    @Test
    void decode_ValidToken_ReturnsJwt() {
        IntrospectResponse response = IntrospectResponse.builder().valid(true).build();
        when(authenticationService.introspect(any(IntrospectRequest.class))).thenReturn(response);

        Jwt mockJwt = org.mockito.Mockito.mock(Jwt.class);
        when(nimbusJwtDecoder.decode(VALID_TOKEN)).thenReturn(mockJwt);

        Jwt decodedJwt = customJwtDecoder.decode(VALID_TOKEN);
        
        assertEquals(mockJwt, decodedJwt);
    }

    @Test
    void decode_InvalidToken_ThrowsBadJwtException() {
        IntrospectResponse response = IntrospectResponse.builder().valid(false).build();
        when(authenticationService.introspect(any(IntrospectRequest.class))).thenReturn(response);

        assertThrows(BadJwtException.class, () -> customJwtDecoder.decode(INVALID_TOKEN));
    }

    @Test
    void decode_AuthenticationServiceThrowsException_ThrowsBadJwtException() {
        when(authenticationService.introspect(any(IntrospectRequest.class))).thenThrow(new RuntimeException("DB Error"));

        assertThrows(BadJwtException.class, () -> customJwtDecoder.decode(INVALID_TOKEN));
    }
    
    @Test
    void decode_NimbusThrowsException_ThrowsBadJwtException() {
        IntrospectResponse response = IntrospectResponse.builder().valid(true).build();
        when(authenticationService.introspect(any(IntrospectRequest.class))).thenReturn(response);
        when(nimbusJwtDecoder.decode(VALID_TOKEN)).thenThrow(new BadJwtException("Invalid signature"));

        assertThrows(BadJwtException.class, () -> customJwtDecoder.decode(VALID_TOKEN));
    }
}
