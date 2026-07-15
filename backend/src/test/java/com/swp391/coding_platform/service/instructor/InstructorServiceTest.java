package com.swp391.coding_platform.service.instructor;

import com.swp391.coding_platform.dto.response.*;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.course.EnrollmentEntity;
import com.swp391.coding_platform.entity.enums.InstructorStatus;
import com.swp391.coding_platform.entity.instructor.InstructorEntity;
import com.swp391.coding_platform.entity.payment.OrderEntity;
import com.swp391.coding_platform.entity.payment.OrderItemEntity;
import com.swp391.coding_platform.entity.payment.PayoutRequestEntity;
import com.swp391.coding_platform.entity.payment.WalletEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.repository.course.CourseRepository;
import com.swp391.coding_platform.repository.course.EnrollmentRepository;
import com.swp391.coding_platform.repository.instructor.InstructorRepository;
import com.swp391.coding_platform.repository.payment.OrderItemRepository;
import com.swp391.coding_platform.repository.payment.PayoutRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstructorServiceTest {

    @Mock
    private InstructorRepository instructorRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private PayoutRequestRepository payoutRequestRepository;

    @InjectMocks
    private InstructorService instructorService;

    private InstructorEntity activeInstructor;
    private InstructorEntity suspendedInstructor;
    private UserEntity instructorUser;

    @BeforeEach
    void setUp() {
        instructorUser = new UserEntity();
        instructorUser.setId(10);
        instructorUser.setDisplayname("John Instructor");
        instructorUser.setAvatarurl("https://example.com/avatar.jpg");

        WalletEntity wallet = new WalletEntity();
        wallet.setId(100);
        instructorUser.setWallet(wallet);

        activeInstructor = new InstructorEntity();
        activeInstructor.setId(1);
        activeInstructor.setStatus(InstructorStatus.ACTIVE);
        activeInstructor.setUser(instructorUser);

        suspendedInstructor = new InstructorEntity();
        suspendedInstructor.setId(2);
        suspendedInstructor.setStatus(InstructorStatus.SUSPENDED);
        suspendedInstructor.setUser(instructorUser);
    }

    // ======================== getRevenueSummary ========================

    @Test
    void getRevenueSummary_ShouldReturnSummary() {
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(activeInstructor));
        when(orderItemRepository.findCompletedItemsByInstructorId(1)).thenReturn(Collections.emptyList());

        InstructorRevenueSummary summary = instructorService.getRevenueSummary(1, "all", null, null);

        assertNotNull(summary);
        assertEquals(BigDecimal.ZERO, summary.getTotalGrossRevenue());
    }

    @Test
    void getRevenueSummary_InstructorSuspended_ShouldThrowException() {
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(suspendedInstructor));

        AppException ex = assertThrows(AppException.class, () ->
                instructorService.getRevenueSummary(1, "all", null, null));
        assertEquals(ErrorCode.ACCESS_DENIED, ex.getErrorCode());
    }

    @Test
    void getRevenueSummary_InstructorNotFound_ShouldThrowException() {
        when(instructorRepository.findByUserId(999)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
                instructorService.getRevenueSummary(999, "all", null, null));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getRevenueSummary_WithThisMonthFilter_ShouldReturnFilteredRevenue() {
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(activeInstructor));
        when(orderItemRepository.findCompletedItemsByInstructorId(1)).thenReturn(Collections.emptyList());

        InstructorRevenueSummary summary = instructorService.getRevenueSummary(1, "this-month", null, null);

        assertNotNull(summary);
        assertEquals(BigDecimal.ZERO, summary.getTotalGrossRevenue());
    }

    @Test
    void getRevenueSummary_WithLastMonthFilter_ShouldReturnFilteredRevenue() {
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(activeInstructor));
        when(orderItemRepository.findCompletedItemsByInstructorId(1)).thenReturn(Collections.emptyList());

        InstructorRevenueSummary summary = instructorService.getRevenueSummary(1, "last-month", null, null);

        assertNotNull(summary);
        assertEquals(BigDecimal.ZERO, summary.getTotalGrossRevenue());
    }

    @Test
    void getRevenueSummary_WithCustomDateFilter_ShouldReturnFilteredRevenue() {
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(activeInstructor));
        when(orderItemRepository.findCompletedItemsByInstructorId(1)).thenReturn(Collections.emptyList());

        InstructorRevenueSummary summary = instructorService.getRevenueSummary(1, "custom", "2025-01-01", "2025-12-31");

        assertNotNull(summary);
    }

    // ======================== getSalesHistory ========================

    @Test
    void getSalesHistory_InstructorNotFound_ShouldThrowException() {
        when(instructorRepository.findByUserId(999)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
                instructorService.getSalesHistory(999, "all", null, null));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getSalesHistory_InstructorSuspended_ShouldThrowException() {
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(suspendedInstructor));

        AppException ex = assertThrows(AppException.class, () ->
                instructorService.getSalesHistory(1, "all", null, null));
        assertEquals(ErrorCode.ACCESS_DENIED, ex.getErrorCode());
    }

    @Test
    void getSalesHistory_EmptyItems_ShouldReturnEmptyList() {
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(activeInstructor));
        when(orderItemRepository.findCompletedItemsByInstructorId(1)).thenReturn(Collections.emptyList());

        List<SalesHistoryItem> result = instructorService.getSalesHistory(1, "all", null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ======================== getRecentRegistrations ========================

    @Test
    void getRecentRegistrations_InstructorNotFound_ShouldThrowException() {
        when(instructorRepository.findByUserId(999)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
                instructorService.getRecentRegistrations(999));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getRecentRegistrations_InstructorSuspended_ShouldThrowException() {
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(suspendedInstructor));

        AppException ex = assertThrows(AppException.class, () ->
                instructorService.getRecentRegistrations(1));
        assertEquals(ErrorCode.ACCESS_DENIED, ex.getErrorCode());
    }

    @Test
    void getRecentRegistrations_NoEnrollments_ShouldReturnEmptyList() {
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(activeInstructor));
        when(enrollmentRepository.findEnrollmentsByInstructorId(1)).thenReturn(Collections.emptyList());

        List<RecentRegistration> result = instructorService.getRecentRegistrations(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ======================== getPayoutHistory ========================

    @Test
    void getPayoutHistory_InstructorNotFound_ShouldThrowException() {
        when(instructorRepository.findByUserId(999)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
                instructorService.getPayoutHistory(999));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getPayoutHistory_InstructorSuspended_ShouldThrowException() {
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(suspendedInstructor));

        AppException ex = assertThrows(AppException.class, () ->
                instructorService.getPayoutHistory(1));
        assertEquals(ErrorCode.ACCESS_DENIED, ex.getErrorCode());
    }

    @Test
    void getPayoutHistory_WalletNull_ShouldReturnEmptyList() {
        // Instructor user with no wallet
        UserEntity userNoWallet = new UserEntity();
        userNoWallet.setId(20);
        userNoWallet.setWallet(null);

        InstructorEntity instructorNoWallet = new InstructorEntity();
        instructorNoWallet.setId(3);
        instructorNoWallet.setStatus(InstructorStatus.ACTIVE);
        instructorNoWallet.setUser(userNoWallet);

        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(instructorNoWallet));

        List<PayoutHistoryItem> result = instructorService.getPayoutHistory(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getPayoutHistory_WithPayouts_ShouldReturnPayoutList() {
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(activeInstructor));

        PayoutRequestEntity payout = new PayoutRequestEntity();
        payout.setId(1);
        payout.setPayoutPeriod("2025-01");
        payout.setAmount(new BigDecimal("500000"));
        payout.setBankName("VietcomBank");
        payout.setBankAccountNumber("1234567890");
        payout.setStatus(com.swp391.coding_platform.entity.enums.PayoutStatus.PENDING);
        payout.setTransactionReference(null);
        payout.setAdminNote(null);

        when(payoutRequestRepository.findByWalletIdOrderByCreatedAtDesc(100)).thenReturn(List.of(payout));

        List<PayoutHistoryItem> result = instructorService.getPayoutHistory(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PO-1", result.get(0).getId());
        assertEquals("VietcomBank", result.get(0).getBankName());
    }

    // ======================== getCourseBreakdown ========================

    @Test
    void getCourseBreakdown_InstructorNotFound_ShouldThrowException() {
        when(instructorRepository.findByUserId(999)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
                instructorService.getCourseBreakdown(999, "all", null, null));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getCourseBreakdown_EmptyItems_ShouldReturnZeroPercentages() {
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(activeInstructor));
        when(orderItemRepository.findCompletedItemsByInstructorId(1)).thenReturn(Collections.emptyList());

        CourseEntity course = new CourseEntity();
        course.setId(1L);
        course.setTitle("Java Course");
        when(courseRepository.findByInstructorId(1)).thenReturn(List.of(course));

        List<CourseBreakdownItem> result = instructorService.getCourseBreakdown(1, "all", null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getPercentage()); // No sales → 0%
    }

    @Test
    void getCourseBreakdown_WithSales_ShouldCalculatePercentage() {
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(activeInstructor));

        CourseEntity course = new CourseEntity();
        course.setId(1L);
        course.setTitle("Java Course");
        when(courseRepository.findByInstructorId(1)).thenReturn(List.of(course));

        // Create a mock order item
        OrderEntity order = new OrderEntity();
        order.setCreatedAt(Instant.now());

        OrderItemEntity item = new OrderItemEntity();
        item.setId(1);
        item.setCourse(course);
        item.setPrice(new BigDecimal("100000"));
        item.setOrder(order);

        when(orderItemRepository.findCompletedItemsByInstructorId(1)).thenReturn(List.of(item));

        List<CourseBreakdownItem> result = instructorService.getCourseBreakdown(1, "all", null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getPercentage()); // 100% since only one course
    }

    // ======================== getMonthlyChartData ========================

    @Test
    void getMonthlyChartData_InstructorNotFound_ShouldThrowException() {
        when(instructorRepository.findByUserId(999)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
                instructorService.getMonthlyChartData(999));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getMonthlyChartData_ShouldReturnLast12Months() {
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(activeInstructor));
        when(orderItemRepository.findCompletedItemsByInstructorId(1)).thenReturn(Collections.emptyList());

        List<MonthlyChartItem> result = instructorService.getMonthlyChartData(1);

        assertNotNull(result);
        assertEquals(12, result.size()); // Always returns last 12 months
    }

    // ======================== getCourseRegistrations ========================

    @Test
    void getCourseRegistrations_InstructorNotFound_ShouldThrowException() {
        when(instructorRepository.findByUserId(999)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
                instructorService.getCourseRegistrations(999, "12m"));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getCourseRegistrations_Default12mTimeframe_ShouldReturnAllRegistrations() {
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(activeInstructor));
        when(orderItemRepository.findCompletedItemsByInstructorId(1)).thenReturn(Collections.emptyList());
        when(courseRepository.findByInstructorId(1)).thenReturn(Collections.emptyList());

        InstructorCourseRegistrationsResponse result = instructorService.getCourseRegistrations(1, "12m");

        assertNotNull(result);
        assertEquals(0, result.getTotalTrendRegistrations());
    }

    @Test
    void getCourseRegistrations_1mTimeframe_ShouldFilter() {
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(activeInstructor));
        when(orderItemRepository.findCompletedItemsByInstructorId(1)).thenReturn(Collections.emptyList());
        when(courseRepository.findByInstructorId(1)).thenReturn(Collections.emptyList());

        InstructorCourseRegistrationsResponse result = instructorService.getCourseRegistrations(1, "1m");

        assertNotNull(result);
        assertEquals(0, result.getTotalTrendRegistrations());
    }

    @Test
    void getCourseRegistrations_3mTimeframe_ShouldFilter() {
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(activeInstructor));
        when(orderItemRepository.findCompletedItemsByInstructorId(1)).thenReturn(Collections.emptyList());
        when(courseRepository.findByInstructorId(1)).thenReturn(Collections.emptyList());

        InstructorCourseRegistrationsResponse result = instructorService.getCourseRegistrations(1, "3m");

        assertNotNull(result);
        assertEquals(0, result.getTotalTrendRegistrations());
    }

    @Test
    void getCourseRegistrations_9mTimeframe_ShouldFilter() {
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(activeInstructor));
        when(orderItemRepository.findCompletedItemsByInstructorId(1)).thenReturn(Collections.emptyList());
        when(courseRepository.findByInstructorId(1)).thenReturn(Collections.emptyList());

        InstructorCourseRegistrationsResponse result = instructorService.getCourseRegistrations(1, "9m");

        assertNotNull(result);
        assertEquals(0, result.getTotalTrendRegistrations());
    }

    // ======================== getAllInstructorsForAdmin ========================

    @Test
    void getAllInstructorsForAdmin_ShouldReturnList() {
        when(instructorRepository.findAll()).thenReturn(List.of(activeInstructor));
        when(courseRepository.findByInstructorId(1)).thenReturn(Collections.emptyList());

        List<AdminInstructorResponse> responses = instructorService.getAllInstructorsForAdmin();

        assertEquals(1, responses.size());
        assertEquals(10, responses.get(0).getUserId());
    }

    // ======================== updateInstructorStatus ========================

    @Test
    void updateInstructorStatus_ShouldUpdateAndReturn() {
        when(instructorRepository.findById(1)).thenReturn(Optional.of(activeInstructor));
        when(instructorRepository.save(any(InstructorEntity.class))).thenReturn(activeInstructor);
        when(courseRepository.findByInstructorId(1)).thenReturn(Collections.emptyList());

        AdminInstructorResponse response = instructorService.updateInstructorStatus(1, "SUSPENDED");

        assertEquals("SUSPENDED", response.getStatus());
        verify(instructorRepository).save(activeInstructor);
    }

    @Test
    void updateInstructorStatus_InstructorNotFound_ShouldThrowException() {
        when(instructorRepository.findById(999)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
                instructorService.updateInstructorStatus(999, "SUSPENDED"));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void updateInstructorStatus_InvalidStatusString_ShouldThrowRuntimeException() {
        when(instructorRepository.findById(1)).thenReturn(Optional.of(activeInstructor));

        // Invalid status → InstructorStatus.valueOf() throws IllegalArgumentException
        assertThrows(RuntimeException.class, () ->
                instructorService.updateInstructorStatus(1, "INVALID_STATUS"));
    }
}
