package com.swp391.coding_platform.controller.cart;

import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.service.cart.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Long>>> getCartCourseIds(@AuthenticationPrincipal Jwt jwt) {
        String username = getUsername(jwt);
        List<Long> courseIds = cartService.getCartCourseIds(username);
        return ResponseEntity.ok(ApiResponse.<List<Long>>builder()
                .code(200)
                .message("Cart fetched successfully")
                .result(courseIds)
                .build());
    }

    @PostMapping("/{courseId}")
    public ResponseEntity<ApiResponse<Void>> addToCart(@AuthenticationPrincipal Jwt jwt, @PathVariable("courseId") Long courseId) {
        String username = getUsername(jwt);
        cartService.addToCart(username, courseId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Course added to cart successfully")
                .build());
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(@AuthenticationPrincipal Jwt jwt, @PathVariable("courseId") Long courseId) {
        String username = getUsername(jwt);
        cartService.removeFromCart(username, courseId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Course removed from cart successfully")
                .build());
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal Jwt jwt) {
        String username = getUsername(jwt);
        cartService.clearCart(username);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Cart cleared successfully")
                .build());
    }

    private String getUsername(Jwt jwt) {
        if (jwt.hasClaim("preferred_username")) {
            return jwt.getClaimAsString("preferred_username");
        }
        return jwt.getSubject();
    }
}
