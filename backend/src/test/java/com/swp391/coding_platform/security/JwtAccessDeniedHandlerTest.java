package com.swp391.coding_platform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtAccessDeniedHandlerTest {

    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @BeforeEach
    void setUp() {
        jwtAccessDeniedHandler = new JwtAccessDeniedHandler();
    }

    @Test
    void handle_ShouldReturn403ForbiddenAndErrorJson() throws IOException, jakarta.servlet.ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedException exception = new AccessDeniedException("Access is denied");

        jwtAccessDeniedHandler.handle(request, response, exception);

        assertEquals(403, response.getStatus());
        assertEquals("application/json", response.getContentType());

        String jsonResponse = response.getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        
        ApiResponse<?> apiResponse = mapper.readValue(jsonResponse, ApiResponse.class);
        
        assertNotNull(apiResponse);
        assertEquals(ErrorCode.ACCESS_DENIED.getCode(), apiResponse.getCode());
        assertEquals(ErrorCode.ACCESS_DENIED.getMessage(), apiResponse.getMessage());
    }
}
