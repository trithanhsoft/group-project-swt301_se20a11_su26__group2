package com.swp391.coding_platform.service.instructor;

import com.swp391.coding_platform.dto.request.ApproveApplicationRequest;
import com.swp391.coding_platform.dto.request.InstructorApplyRequest;
import com.swp391.coding_platform.dto.response.InstructorApplicationResponse;
import com.swp391.coding_platform.entity.auth.RoleEntity;
import com.swp391.coding_platform.entity.enums.InstructorAppStatus;
import com.swp391.coding_platform.entity.enums.InstructorStatus;
import com.swp391.coding_platform.entity.enums.RoleName;
import com.swp391.coding_platform.entity.enums.UserStatus;
import com.swp391.coding_platform.entity.instructor.InstructorApplicationEntity;
import com.swp391.coding_platform.entity.instructor.InstructorEntity;
import com.swp391.coding_platform.entity.payment.WalletEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.repository.auth.RoleRepository;
import com.swp391.coding_platform.repository.instructor.InstructorApplicationRepository;
import com.swp391.coding_platform.repository.instructor.InstructorRepository;
import com.swp391.coding_platform.repository.payment.WalletRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstructorApplicationServiceTest {

    @Mock
    private InstructorApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private InstructorRepository instructorRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private InstructorApplicationService applicationService;

    @Test
    void apply_UserNotFound_ShouldThrowException() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        InstructorApplyRequest request = new InstructorApplyRequest();
        assertThrows(AppException.class, () -> applicationService.apply(1, request));
    }

    @Test
    void apply_UserAlreadyInstructor_ShouldThrowException() {
        UserEntity user = new UserEntity();
        RoleEntity role = new RoleEntity();
        role.setName(RoleName.INSTRUCTOR);
        HashSet<RoleEntity> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        InstructorApplyRequest request = new InstructorApplyRequest();
        assertThrows(AppException.class, () -> applicationService.apply(1, request));
    }

    @Test
    void apply_Success() {
        UserEntity user = UserEntity.builder()
                .id(1)
                .username("student")
                .roles(new HashSet<>())
                .build();

        InstructorApplyRequest request = InstructorApplyRequest.builder()
                .fullName("John Doe")
                .major("Computer Science")
                .bio("I love teaching")
                .build();

        RoleEntity role = RoleEntity.builder().name(RoleName.INSTRUCTOR).build();
        InstructorApplicationEntity appEntity = InstructorApplicationEntity.builder()
                .id(50)
                .user(user)
                .cvUrl("self_registered")
                .introduction("I love teaching")
                .status(InstructorAppStatus.APPROVED)
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(RoleName.INSTRUCTOR)).thenReturn(Optional.of(role));
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.empty());
        when(instructorRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(applicationRepository.save(any())).thenReturn(appEntity);

        InstructorApplicationResponse response = applicationService.apply(1, request);

        assertNotNull(response);
        assertEquals(50, response.getId());
        assertEquals("APPROVED", response.getStatus());
        verify(userRepository, times(1)).save(user);
        verify(walletRepository, times(1)).save(any());
        verify(applicationRepository, times(1)).save(any());
    }

    @Test
    void getMyApplicationStatus_Empty_ReturnsNull() {
        when(applicationRepository.findByUserIdOrderByCreatedAtDesc(1)).thenReturn(Collections.emptyList());
        assertNull(applicationService.getMyApplicationStatus(1));
    }

    @Test
    void getMyApplicationStatus_NotEmpty_ReturnsLatest() {
        UserEntity user = UserEntity.builder().id(1).build();
        InstructorApplicationEntity app = InstructorApplicationEntity.builder()
                .id(2)
                .user(user)
                .status(InstructorAppStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        when(applicationRepository.findByUserIdOrderByCreatedAtDesc(1)).thenReturn(List.of(app));

        InstructorApplicationResponse response = applicationService.getMyApplicationStatus(1);
        assertNotNull(response);
        assertEquals(2, response.getId());
        assertEquals("PENDING", response.getStatus());
    }

    @Test
    void approveApplication_NotFound_ThrowsException() {
        when(applicationRepository.findById(1)).thenReturn(Optional.empty());

        ApproveApplicationRequest req = new ApproveApplicationRequest();
        req.setStatus("APPROVED");

        AppException ex = assertThrows(AppException.class, () -> applicationService.approveApplication(1, req));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void approveApplication_NotPending_ThrowsException() {
        InstructorApplicationEntity app = InstructorApplicationEntity.builder()
                .id(1)
                .status(InstructorAppStatus.APPROVED)
                .build();

        when(applicationRepository.findById(1)).thenReturn(Optional.of(app));

        ApproveApplicationRequest req = new ApproveApplicationRequest();
        req.setStatus("APPROVED");

        assertThrows(RuntimeException.class, () -> applicationService.approveApplication(1, req));
    }

    @Test
    void approveApplication_InvalidStatus_ThrowsException() {
        InstructorApplicationEntity app = InstructorApplicationEntity.builder()
                .id(1)
                .status(InstructorAppStatus.PENDING)
                .build();

        when(applicationRepository.findById(1)).thenReturn(Optional.of(app));

        ApproveApplicationRequest req = new ApproveApplicationRequest();
        req.setStatus("INVALID_STATUS_NAME");

        assertThrows(RuntimeException.class, () -> applicationService.approveApplication(1, req));
    }

    @Test
    void approveApplication_ShouldApprove() {
        InstructorApplicationEntity app = new InstructorApplicationEntity();
        app.setId(1);
        app.setStatus(InstructorAppStatus.PENDING);
        app.setIntroduction("intro");
        
        UserEntity user = new UserEntity();
        user.setId(10);
        user.setUsername("testuser");
        user.setRoles(new HashSet<>());
        app.setUser(user);

        when(applicationRepository.findById(1)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any())).thenReturn(app);
        
        RoleEntity role = new RoleEntity();
        role.setName(RoleName.INSTRUCTOR);
        when(roleRepository.findByName(RoleName.INSTRUCTOR)).thenReturn(Optional.of(role));

        ApproveApplicationRequest req = new ApproveApplicationRequest();
        req.setStatus("APPROVED");
        req.setAdminNote("Looks good");

        InstructorApplicationResponse res = applicationService.approveApplication(1, req);

        assertEquals("APPROVED", res.getStatus());
        verify(userRepository, times(1)).save(user);
        verify(instructorRepository, times(1)).save(any());
        verify(walletRepository, times(1)).save(any());
    }

    @Test
    void cleanupRejectedApplications_Success() {
        UserEntity user = UserEntity.builder().id(1).build();
        InstructorApplicationEntity app = InstructorApplicationEntity.builder()
                .id(100)
                .user(user)
                .cvUrl("/uploads/cvs/nonexistent_test_cv.pdf")
                .status(InstructorAppStatus.REJECTED)
                .build();

        when(applicationRepository.findByStatusAndUpdatedAtBefore(eq(InstructorAppStatus.REJECTED), any(Instant.class)))
                .thenReturn(List.of(app));

        applicationService.cleanupRejectedApplications();

        verify(applicationRepository, times(1)).deleteAll(anyList());
    }
}
