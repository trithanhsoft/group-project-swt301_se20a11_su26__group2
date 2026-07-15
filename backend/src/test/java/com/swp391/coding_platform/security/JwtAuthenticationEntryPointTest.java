package com.swp391.coding_platform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class JwtAuthenticationEntryPointTest {

    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @BeforeEach
    void setUp() {
        jwtAuthenticationEntryPoint = new JwtAuthenticationEntryPoint();
    }

    @Test
    void commence_ShouldReturn401UnauthorizedAndErrorJson() throws IOException, jakarta.servlet.ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException exception = mock(AuthenticationException.class);

        jwtAuthenticationEntryPoint.commence(request, response, exception);

        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());

        String jsonResponse = response.getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        
        ApiResponse<?> apiResponse = mapper.readValue(jsonResponse, ApiResponse.class);
        
        assertNotNull(apiResponse);
        assertEquals(ErrorCode.UNAUTHENTICATED.getCode(), apiResponse.getCode());
        assertEquals(ErrorCode.UNAUTHENTICATED.getMessage(), apiResponse.getMessage());
    }
}
