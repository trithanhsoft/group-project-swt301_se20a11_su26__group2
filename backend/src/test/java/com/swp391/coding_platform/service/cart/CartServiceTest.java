package com.swp391.coding_platform.service.cart;

import com.swp391.coding_platform.entity.cart.CartEntity;
import com.swp391.coding_platform.entity.cart.CartItemEntity;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.repository.cart.CartItemRepository;
import com.swp391.coding_platform.repository.cart.CartRepository;
import com.swp391.coding_platform.repository.course.CourseRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CartService cartService;

    private UserEntity user;
    private CartEntity cart;
    private CourseEntity course;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(1);
        user.setUsername("testuser");

        cart = new CartEntity(user);
        cart.setId(1);
        cart.setItems(new ArrayList<>());

        course = new CourseEntity();
        course.setId(10L);
    }

    @Test
    void getCartCourseIds_UserFoundAndCartExists_ReturnsCourseIds() {
        CartItemEntity cartItem = new CartItemEntity(cart, course);
        cart.getItems().add(cartItem);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));

        List<Long> courseIds = cartService.getCartCourseIds("testuser");

        assertEquals(1, courseIds.size());
        assertEquals(10L, courseIds.get(0));
    }

    @Test
    void getCartCourseIds_CartNotFound_ReturnsEmptyList() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1)).thenReturn(Optional.empty());

        List<Long> courseIds = cartService.getCartCourseIds("testuser");

        assertTrue(courseIds.isEmpty());
    }

    @Test
    void getCartCourseIds_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.getCartCourseIds("testuser");
        });
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void addToCart_CartExistsAndItemNotInCart_AddsItem() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndCourseId(cart.getId(), course.getId())).thenReturn(Optional.empty());
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        cartService.addToCart("testuser", course.getId());

        verify(cartItemRepository, times(1)).save(any(CartItemEntity.class));
    }

    @Test
    void addToCart_CartDoesNotExist_CreatesCartAndAddsItem() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1)).thenReturn(Optional.empty());
        when(cartRepository.save(any(CartEntity.class))).thenReturn(cart);
        when(cartItemRepository.findByCartIdAndCourseId(cart.getId(), course.getId())).thenReturn(Optional.empty());
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        cartService.addToCart("testuser", course.getId());

        verify(cartRepository, times(1)).save(any(CartEntity.class));
        verify(cartItemRepository, times(1)).save(any(CartItemEntity.class));
    }

    @Test
    void addToCart_ItemAlreadyInCart_DoesNotAdd() {
        CartItemEntity cartItem = new CartItemEntity(cart, course);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndCourseId(cart.getId(), course.getId())).thenReturn(Optional.of(cartItem));

        cartService.addToCart("testuser", course.getId());

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void removeFromCart_ItemInCart_RemovesItem() {
        CartItemEntity cartItem = new CartItemEntity(cart, course);
        cart.getItems().add(cartItem);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));

        cartService.removeFromCart("testuser", course.getId());

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void clearCart_ItemsInCart_ClearsCart() {
        CartItemEntity cartItem = new CartItemEntity(cart, course);
        cart.getItems().add(cartItem);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));

        cartService.clearCart("testuser");

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository, times(1)).save(cart);
    }
}
