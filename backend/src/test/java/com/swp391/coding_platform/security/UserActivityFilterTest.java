package com.swp391.coding_platform.security;

import com.swp391.coding_platform.repository.user.UserDailyActivityRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserActivityFilterTest {

    @Mock
    private UserDailyActivityRepository activityRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private UserActivityFilter userActivityFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithValidJwt_ShouldTrackActivity() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("userId")).thenReturn(100L);
        JwtAuthenticationToken jwtAuthToken = new JwtAuthenticationToken(jwt);
        
        when(securityContext.getAuthentication()).thenReturn(jwtAuthToken);

        // Act
        userActivityFilter.doFilter(request, response, filterChain);

        // Assert
        verify(activityRepository, times(1)).trackActivity(100);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithSameDateCache_ShouldNotTrackActivityTwice() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("userId")).thenReturn(200L);
        JwtAuthenticationToken jwtAuthToken = new JwtAuthenticationToken(jwt);
        
        when(securityContext.getAuthentication()).thenReturn(jwtAuthToken);

        // Inject cache state
        Field cacheField = UserActivityFilter.class.getDeclaredField("userLastTrackedDate");
        cacheField.setAccessible(true);
        Map<Integer, LocalDate> cache = (Map<Integer, LocalDate>) cacheField.get(userActivityFilter);
        cache.put(200, LocalDate.now()); // Already tracked today

        // Act
        userActivityFilter.doFilter(request, response, filterChain);

        // Assert
        verify(activityRepository, never()).trackActivity(anyInt());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_NoJwtAuthentication_ShouldProceedWithoutTracking() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(securityContext.getAuthentication()).thenReturn(null); // Not a JwtAuthenticationToken

        // Act
        userActivityFilter.doFilter(request, response, filterChain);

        // Assert
        verify(activityRepository, never()).trackActivity(anyInt());
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
