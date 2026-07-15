package com.swp391.coding_platform.controller.cart;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import com.swp391.coding_platform.repository.user.UserDailyActivityRepository;

import com.swp391.coding_platform.service.cart.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private UserDailyActivityRepository userDailyActivityRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Test
    void getCartCourseIds_ReturnsCourseIds() throws Exception {
        List<Long> courseIds = Arrays.asList(1L, 2L);
        when(cartService.getCartCourseIds("testuser")).thenReturn(courseIds);

        mockMvc.perform(get("/cart")
                .with(jwt().jwt(builder -> builder.claim("preferred_username", "testuser"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Cart fetched successfully"))
                .andExpect(jsonPath("$.result[0]").value(1))
                .andExpect(jsonPath("$.result[1]").value(2));

        verify(cartService, times(1)).getCartCourseIds("testuser");
    }

    @Test
    void addToCart_ReturnsSuccess() throws Exception {
        doNothing().when(cartService).addToCart(anyString(), anyLong());

        mockMvc.perform(post("/cart/10")
                .with(jwt().jwt(builder -> builder.claim("preferred_username", "testuser")))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Course added to cart successfully"));

        verify(cartService, times(1)).addToCart("testuser", 10L);
    }

    @Test
    void removeFromCart_ReturnsSuccess() throws Exception {
        doNothing().when(cartService).removeFromCart(anyString(), anyLong());

        mockMvc.perform(delete("/cart/10")
                .with(jwt().jwt(builder -> builder.claim("preferred_username", "testuser")))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Course removed from cart successfully"));

        verify(cartService, times(1)).removeFromCart("testuser", 10L);
    }

    @Test
    void clearCart_ReturnsSuccess() throws Exception {
        doNothing().when(cartService).clearCart(anyString());

        mockMvc.perform(delete("/cart/clear")
                .with(jwt().jwt(builder -> builder.claim("preferred_username", "testuser")))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Cart cleared successfully"));

        verify(cartService, times(1)).clearCart("testuser");
    }
}
