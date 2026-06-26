package com.swp391.coding_platform.security;

import com.swp391.coding_platform.repository.user.UserDailyActivityRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActivityFilter extends OncePerRequestFilter {

    private final UserDailyActivityRepository activityRepository;
    
    // In-memory cache to prevent spamming the database
    // Key: userId, Value: last tracked date
    private final Map<Integer, LocalDate> userLastTrackedDate = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
            try {
                Jwt jwt = jwtAuth.getToken();
                Number idClaim = jwt.getClaim("userId");
                
                if (idClaim != null) {
                    int userId = idClaim.intValue();
                    LocalDate today = LocalDate.now();
                    LocalDate lastTracked = userLastTrackedDate.get(userId);

                    // Only hit the database if we haven't tracked this user today
                    if (lastTracked == null || !lastTracked.equals(today)) {
                        activityRepository.trackActivity(userId);
                        // Update cache after successful DB insert
                        userLastTrackedDate.put(userId, today);
                    }
                }
                
            } catch (Exception e) {
                log.error("Error tracking user daily activity: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
