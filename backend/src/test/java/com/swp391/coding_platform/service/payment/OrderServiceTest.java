package com.swp391.coding_platform.service.payment;

import com.swp391.coding_platform.dto.request.OrderCheckoutRequest;
import com.swp391.coding_platform.dto.response.OrderCheckoutResponse;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.enums.CourseStatus;
import com.swp391.coding_platform.entity.enums.OrderStatus;
import com.swp391.coding_platform.entity.payment.OrderEntity;
import com.swp391.coding_platform.entity.payment.WalletEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.repository.course.CourseRepository;
import com.swp391.coding_platform.repository.course.EnrollmentRepository;
import com.swp391.coding_platform.repository.payment.OrderRepository;
import com.swp391.coding_platform.repository.payment.WalletRepository;
import com.swp391.coding_platform.repository.payment.WalletTransactionRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createCheckout_HappyPath_ReturnsOrderCheckoutResponse() {
        Integer userId = 1;
        OrderCheckoutRequest request = new OrderCheckoutRequest();
        request.setCourseIds(List.of(1L));

        UserEntity user = new UserEntity();
        user.setId(userId);
        
        CourseEntity course = new CourseEntity();
        course.setId(1L);
        course.setPrice(new BigDecimal("100.00"));
        course.setStatus(CourseStatus.APPROVED);

        WalletEntity wallet = new WalletEntity();
        wallet.setBalance(new BigDecimal("200.00"));

        OrderEntity savedOrder = new OrderEntity();
        savedOrder.setId(1);
        savedOrder.setStatus(OrderStatus.COMPLETED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findAllById(anyList())).thenReturn(List.of(course));
        when(enrollmentRepository.findEnrolledCourseIdsByUserIdAndCourseIds(anyInt(), anyList(), anyList())).thenReturn(Collections.emptySet());
        when(walletRepository.findByUserIdWithLock(userId)).thenReturn(Optional.of(wallet));
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);

        OrderCheckoutResponse response = orderService.createCheckout(userId, request);

        assertNotNull(response);
        assertEquals(1, response.getOrderId());
        assertEquals(OrderStatus.COMPLETED, response.getStatus());
        assertEquals(new BigDecimal("100.00"), response.getTotalAmount());
        
        verify(walletRepository).save(wallet);
        verify(walletTransactionRepository).save(any());
        verify(enrollmentRepository).saveAll(anyList());
        verify(courseRepository).incrementTotalEnrolledForCourses(anyList());
    }

    @Test
    void createCheckout_InsufficientBalance_ThrowsAppException() {
        Integer userId = 1;
        OrderCheckoutRequest request = new OrderCheckoutRequest();
        request.setCourseIds(List.of(1L));

        UserEntity user = new UserEntity();
        user.setId(userId);

        CourseEntity course = new CourseEntity();
        course.setId(1L);
        course.setPrice(new BigDecimal("100.00"));
        course.setStatus(CourseStatus.APPROVED);

        WalletEntity wallet = new WalletEntity();
        wallet.setBalance(new BigDecimal("50.00"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findAllById(anyList())).thenReturn(List.of(course));
        when(enrollmentRepository.findEnrolledCourseIdsByUserIdAndCourseIds(anyInt(), anyList(), anyList())).thenReturn(Collections.emptySet());
        when(walletRepository.findByUserIdWithLock(userId)).thenReturn(Optional.of(wallet));

        assertThrows(AppException.class, () -> orderService.createCheckout(userId, request));
    }
}
