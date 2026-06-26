package com.swp391.coding_platform.security;

import com.swp391.coding_platform.dto.request.IntrospectRequest;
import com.swp391.coding_platform.dto.response.IntrospectResponse;
import com.swp391.coding_platform.service.auth.AuthenticationService;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomJwtDecoder implements JwtDecoder {

    @Lazy
    AuthenticationService authenticationService;

    @NonFinal
    NimbusJwtDecoder nimbusJwtDecoder;

    @NonFinal
    @Value("${jwt.signer-key}")
    String signerKey;

    @PostConstruct
    void init() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                signerKey.getBytes(StandardCharsets.UTF_8),
                "HmacSHA512"
        );

        this.nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            IntrospectResponse introspectResponse = authenticationService.introspect(
                    IntrospectRequest.builder()
                            .token(token)
                            .build()
            );

            if (!introspectResponse.isValid()) {
                throw new BadJwtException("Token invalid");
            }

            return nimbusJwtDecoder.decode(token);
        } catch (BadJwtException exception) {
            throw exception;
        } catch (JwtException exception) {
            throw new BadJwtException("Token invalid", exception);
        } catch (Exception exception) {
            throw new BadJwtException("Token introspection failed", exception);
        }
    }
}