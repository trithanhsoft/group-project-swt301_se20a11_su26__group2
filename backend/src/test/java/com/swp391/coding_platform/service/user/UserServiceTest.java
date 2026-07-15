package com.swp391.coding_platform.service.user;

import com.swp391.coding_platform.dto.request.AppealRequest;
import com.swp391.coding_platform.dto.request.ChangePasswordRequest;
import com.swp391.coding_platform.dto.request.LockUserRequest;
import com.swp391.coding_platform.dto.response.AdminUserResponse;
import com.swp391.coding_platform.dto.response.UserResponse;
import com.swp391.coding_platform.entity.enums.OrderStatus;
import com.swp391.coding_platform.entity.enums.StatusTransaction;
import com.swp391.coding_platform.entity.enums.TransactionType;
import com.swp391.coding_platform.entity.enums.UserStatus;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.entity.payment.WalletEntity;
import com.swp391.coding_platform.entity.payment.OrderItemEntity;
import com.swp391.coding_platform.entity.payment.OrderEntity;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.mapper.UserMapper;
import com.swp391.coding_platform.repository.payment.OrderItemRepository;
import com.swp391.coding_platform.repository.payment.WalletTransactionRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private WalletTransactionRepository walletTransactionRepository;
    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getMyInfo_Success() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("testuser");

        UserResponse userResponse = new UserResponse();
        userResponse.setDisplayName("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
        when(userMapper.toUserResponse(userEntity)).thenReturn(userResponse);

        UserResponse response = userService.getMyInfo("testuser");

        assertNotNull(response);
        assertEquals("testuser", response.getDisplayName());
    }

    @Test
    void getMyInfo_UserNotFound_ThrowsAppException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> userService.getMyInfo("testuser"));
    }

    @Test
    void changePassword_Success() {
        ChangePasswordRequest request = new ChangePasswordRequest("oldpass", "newpass", "newpass");
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("testuser");
        userEntity.setPasswordHash("hashed_oldpass");
        userEntity.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches("oldpass", "hashed_oldpass")).thenReturn(true);
        when(passwordEncoder.matches("newpass", "hashed_oldpass")).thenReturn(false);
        when(passwordEncoder.encode("newpass")).thenReturn("hashed_newpass");

        userService.changePassword("testuser", request);

        verify(userRepository, times(1)).save(userEntity);
        assertEquals("hashed_newpass", userEntity.getPasswordHash());
    }

    @Test
    void changePassword_OldPasswordNotMatch_ThrowsAppException() {
        ChangePasswordRequest request = new ChangePasswordRequest("wrongold", "newpass", "newpass");
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("testuser");
        userEntity.setPasswordHash("hashed_oldpass");
        userEntity.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches("wrongold", "hashed_oldpass")).thenReturn(false);

        assertThrows(AppException.class, () -> userService.changePassword("testuser", request));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void getAllUsersForAdmin_Success() {
        WalletEntity wallet = WalletEntity.builder().id(10).balance(BigDecimal.valueOf(500)).build();
        UserEntity user = UserEntity.builder()
                .id(1)
                .displayname("User One")
                .email("user1@example.com")
                .createdAt(Instant.now())
                .status(UserStatus.ACTIVE)
                .wallet(wallet)
                .build();

        CourseEntity course = CourseEntity.builder().id(100L).title("Java Course").build();
        OrderEntity order = OrderEntity.builder().createdAt(Instant.now()).build();
        OrderItemEntity orderItem = OrderItemEntity.builder()
                .course(course)
                .price(BigDecimal.valueOf(100))
                .order(order)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(walletTransactionRepository.sumAmountByWalletIdAndTypeAndStatus(10, TransactionType.DEPOSIT, StatusTransaction.SUCCESS))
                .thenReturn(BigDecimal.valueOf(1000));
        when(walletTransactionRepository.sumAmountByWalletIdAndTypeAndStatus(10, TransactionType.BUY_COURSE, StatusTransaction.SUCCESS))
                .thenReturn(BigDecimal.valueOf(500));
        when(orderItemRepository.findByOrderUserIdAndOrderStatus(1, OrderStatus.COMPLETED))
                .thenReturn(List.of(orderItem));

        List<AdminUserResponse> result = userService.getAllUsersForAdmin();

        assertNotNull(result);
        assertEquals(1, result.size());
        AdminUserResponse adminUser = result.get(0);
        assertEquals("User One", adminUser.getName());
        assertEquals(BigDecimal.valueOf(500), adminUser.getBalance());
        assertEquals(BigDecimal.valueOf(1000), adminUser.getTotalDeposited());
        assertEquals(BigDecimal.valueOf(500), adminUser.getTotalPurchased());
        assertEquals(1, adminUser.getPurchasedCourses().size());
        assertEquals("Java Course", adminUser.getPurchasedCourses().get(0).getTitle());
    }

    @Test
    void setUserLockStatus_Lock_Success() {
        UserEntity user = UserEntity.builder()
                .id(1)
                .displayname("User One")
                .email("user1@example.com")
                .createdAt(Instant.now())
                .status(UserStatus.ACTIVE)
                .build();

        LockUserRequest request = new LockUserRequest("LOCKED", "Violation of policy");
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        AdminUserResponse response = userService.setUserLockStatus(1, request);

        assertNotNull(response);
        assertEquals("LOCKED", response.getStatus());
        assertEquals("Violation of policy", response.getLockReason());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void setUserLockStatus_Unlock_Success() {
        UserEntity user = UserEntity.builder()
                .id(1)
                .displayname("User One")
                .email("user1@example.com")
                .createdAt(Instant.now())
                .status(UserStatus.LOCKED)
                .lockReason("Violation")
                .lockAppeal("Please unlock")
                .build();

        LockUserRequest request = new LockUserRequest("ACTIVE", "");
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        AdminUserResponse response = userService.setUserLockStatus(1, request);

        assertNotNull(response);
        assertEquals("ACTIVE", response.getStatus());
        assertNull(response.getLockReason());
        assertNull(response.getLockAppeal());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void setUserLockStatus_InvalidStatus_ThrowsAppException() {
        UserEntity user = UserEntity.builder().id(1).status(UserStatus.ACTIVE).build();
        LockUserRequest request = new LockUserRequest("INVALID_STATUS_NAME", "");
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        AppException ex = assertThrows(AppException.class, () -> userService.setUserLockStatus(1, request));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
    }

    @Test
    void setUserLockStatus_UserNotFound_ThrowsAppException() {
        LockUserRequest request = new LockUserRequest("LOCKED", "Reason");
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> userService.setUserLockStatus(999, request));
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void submitAppeal_Success() {
        UserEntity user = UserEntity.builder()
                .id(1)
                .username("testuser")
                .status(UserStatus.LOCKED)
                .build();

        AppealRequest request = new AppealRequest("This is my appeal");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        userService.submitAppeal("testuser", request);

        assertEquals("This is my appeal", user.getLockAppeal());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void submitAppeal_UserNotLocked_ThrowsAppException() {
        UserEntity user = UserEntity.builder()
                .id(1)
                .username("testuser")
                .status(UserStatus.ACTIVE)
                .build();

        AppealRequest request = new AppealRequest("Appeal");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        AppException ex = assertThrows(AppException.class, () -> userService.submitAppeal("testuser", request));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        verify(userRepository, never()).save(any());
    }
}
