package com.swp391.coding_platform.service.cart;

import com.swp391.coding_platform.entity.cart.CartEntity;
import com.swp391.coding_platform.entity.cart.CartItemEntity;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.repository.cart.CartItemRepository;
import com.swp391.coding_platform.repository.cart.CartRepository;
import com.swp391.coding_platform.repository.course.CourseRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, UserRepository userRepository, CourseRepository courseRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public List<Long> getCartCourseIds(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        CartEntity cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart == null) {
            return List.of();
        }
        return cart.getItems().stream()
                .map(item -> item.getCourse().getId())
                .collect(Collectors.toList());
    }

    @Transactional
    public void addToCart(String username, Long courseId) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        CartEntity cart = cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            CartEntity newCart = new CartEntity(user);
            return cartRepository.save(newCart);
        });

        if (cartItemRepository.findByCartIdAndCourseId(cart.getId(), courseId).isPresent()) {
            return; // Already in cart
        }

        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        CartItemEntity cartItem = new CartItemEntity(cart, course);
        cartItemRepository.save(cartItem);
    }

    @Transactional
    public void removeFromCart(String username, Long courseId) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        CartEntity cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart != null) {
            cart.getItems().removeIf(item -> item.getCourse().getId().equals(courseId));
            cartRepository.save(cart);
        }
    }

    @Transactional
    public void clearCart(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        CartEntity cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart != null) {
            cart.getItems().clear();
            cartRepository.save(cart);
        }
    }
}
