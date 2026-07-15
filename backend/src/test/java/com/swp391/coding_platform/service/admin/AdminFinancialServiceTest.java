package com.swp391.coding_platform.service.admin;

import com.swp391.coding_platform.dto.response.*;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.enums.*;
import com.swp391.coding_platform.entity.instructor.InstructorEntity;
import com.swp391.coding_platform.entity.payment.*;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.repository.payment.OrderItemRepository;
import com.swp391.coding_platform.repository.payment.OrderRepository;
import com.swp391.coding_platform.repository.payment.PayoutRequestRepository;
import com.swp391.coding_platform.repository.payment.WalletTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdminFinancialServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private PayoutRequestRepository payoutRequestRepository;

    @InjectMocks
    private AdminFinancialService adminFinancialService;

    @Test
    void getMonthlyFinancialRecords_Success() {
        OrderEntity order = new OrderEntity();
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.setCreatedAt(Instant.now());
        
        when(orderRepository.findAllByStatusWithDetails(OrderStatus.COMPLETED))
                .thenReturn(Collections.singletonList(order));
        
        when(walletTransactionRepository.findAll())
                .thenReturn(Collections.emptyList());

        List<AdminFinancialMonthlyRecordResponse> result = adminFinancialService.getMonthlyFinancialRecords();

        assertNotNull(result);
        assertEquals(12, result.size());
    }

    @Test
    void getMonthlyFinancialRecords_NoOrders_Success() {
        when(orderRepository.findAllByStatusWithDetails(OrderStatus.COMPLETED))
                .thenReturn(Collections.emptyList());
        
        when(walletTransactionRepository.findAll())
                .thenReturn(Collections.emptyList());

        List<AdminFinancialMonthlyRecordResponse> result = adminFinancialService.getMonthlyFinancialRecords();

        assertNotNull(result);
        assertEquals(12, result.size());
    }

    @Test
    void getTopRevenueCoursesData_Success() {
        InstructorEntity instructor = InstructorEntity.builder().fullName("Dr. Smith").build();
        CourseEntity course = CourseEntity.builder().title("Algorithm").instructor(instructor).build();
        OrderItemEntity item = OrderItemEntity.builder().course(course).price(BigDecimal.valueOf(100000)).build();
        OrderEntity order = OrderEntity.builder().orderItems(List.of(item)).build();

        when(orderRepository.findAllByStatusWithDetails(OrderStatus.COMPLETED))
                .thenReturn(List.of(order));

        List<AdminFinancialTopCourseResponse> topCourses = adminFinancialService.getTopRevenueCoursesData();

        assertNotNull(topCourses);
        assertEquals(1, topCourses.size());
        assertEquals("Algorithm", topCourses.get(0).getName());
        assertEquals(100000, topCourses.get(0).getGross());
        assertEquals(70000, topCourses.get(0).getPayout());
    }

    @Test
    void getFinancialDetails_Success() {
        when(orderRepository.findAllByStatusWithDetails(OrderStatus.COMPLETED)).thenReturn(Collections.emptyList());
        when(walletTransactionRepository.findAll()).thenReturn(Collections.emptyList());

        AdminFinancialDetailsResponse details = adminFinancialService.getFinancialDetails();

        assertNotNull(details);
        assertNotNull(details.getMonthlyBreakdowns());
    }

    @Test
    void getOrdersPage_WithoutDates_Success() {
        UserEntity user = UserEntity.builder().displayname("Alice").email("alice@test.com").build();
        OrderEntity order = OrderEntity.builder()
                .id(1)
                .user(user)
                .totalAmount(BigDecimal.valueOf(100000))
                .createdAt(Instant.now())
                .build();

        Page<OrderEntity> pageObj = new PageImpl<>(List.of(order));
        when(orderRepository.findByStatusOrderByCreatedAtDesc(eq(OrderStatus.COMPLETED), any(Pageable.class)))
                .thenReturn(pageObj);

        PageResponse<AdminFinancialDetailsResponse.OrderDetails> res = adminFinancialService.getOrdersPage(1, 10, null, null);

        assertNotNull(res);
        assertEquals(1, res.getContent().size());
        assertEquals("Alice", res.getContent().get(0).getCustomerName());
    }

    @Test
    void getOrdersPage_WithDates_Success() {
        UserEntity user = UserEntity.builder().displayname("Alice").email("alice@test.com").build();
        OrderEntity order = OrderEntity.builder()
                .id(1)
                .user(user)
                .totalAmount(BigDecimal.valueOf(100000))
                .createdAt(Instant.now())
                .build();

        Page<OrderEntity> pageObj = new PageImpl<>(List.of(order));
        when(orderRepository.findByStatusAndCreatedAtBetweenOrderByCreatedAtDesc(eq(OrderStatus.COMPLETED), any(), any(), any(Pageable.class)))
                .thenReturn(pageObj);

        PageResponse<AdminFinancialDetailsResponse.OrderDetails> res = adminFinancialService.getOrdersPage(1, 10, "2026-01-01", "2026-12-31");

        assertNotNull(res);
        assertEquals(1, res.getContent().size());
    }

    @Test
    void getAwardsPage_WithoutDates_Success() {
        UserEntity user = UserEntity.builder().displayname("Bob").email("bob@test.com").build();
        WalletEntity wallet = WalletEntity.builder().user(user).build();
        WalletTransactionEntity tx = WalletTransactionEntity.builder()
                .id(10)
                .wallet(wallet)
                .amount(BigDecimal.valueOf(50000))
                .createdAt(Instant.now())
                .build();

        Page<WalletTransactionEntity> pageObj = new PageImpl<>(List.of(tx));
        when(walletTransactionRepository.findByTypeAndStatusOrderByCreatedAtDesc(eq(TransactionType.AWARD), eq(StatusTransaction.SUCCESS), any(Pageable.class)))
                .thenReturn(pageObj);

        PageResponse<AdminFinancialDetailsResponse.AwardDetails> res = adminFinancialService.getAwardsPage(1, 10, null, null);

        assertNotNull(res);
        assertEquals(1, res.getContent().size());
        assertEquals("Bob", res.getContent().get(0).getUserName());
    }

    @Test
    void getAwardsPage_WithDates_Success() {
        Page<WalletTransactionEntity> pageObj = new PageImpl<>(Collections.emptyList());
        when(walletTransactionRepository.findByTypeAndStatusAndCreatedAtBetweenOrderByCreatedAtDesc(eq(TransactionType.AWARD), eq(StatusTransaction.SUCCESS), any(), any(), any(Pageable.class)))
                .thenReturn(pageObj);

        PageResponse<AdminFinancialDetailsResponse.AwardDetails> res = adminFinancialService.getAwardsPage(1, 10, "2026-01-01", "2026-12-31");

        assertNotNull(res);
        assertEquals(0, res.getContent().size());
    }

    @Test
    void getSalesPage_WithoutDates_Success() {
        CourseEntity course = CourseEntity.builder().title("SQL").build();
        OrderEntity order = OrderEntity.builder().id(2).createdAt(Instant.now()).build();
        OrderItemEntity item = OrderItemEntity.builder().order(order).course(course).price(BigDecimal.valueOf(150000)).build();

        Page<OrderItemEntity> pageObj = new PageImpl<>(List.of(item));
        when(orderItemRepository.findByOrderStatusOrderByOrderCreatedAtDesc(eq(OrderStatus.COMPLETED), any(Pageable.class)))
                .thenReturn(pageObj);

        PageResponse<AdminFinancialDetailsResponse.SaleDetails> res = adminFinancialService.getSalesPage(1, 10, null, null);

        assertNotNull(res);
        assertEquals(1, res.getContent().size());
        assertEquals("SQL", res.getContent().get(0).getCourseTitle());
    }

    @Test
    void getSalesPage_WithDates_Success() {
        Page<OrderItemEntity> pageObj = new PageImpl<>(Collections.emptyList());
        when(orderItemRepository.findByOrderStatusAndOrderCreatedAtBetweenOrderByOrderCreatedAtDesc(eq(OrderStatus.COMPLETED), any(), any(), any(Pageable.class)))
                .thenReturn(pageObj);

        PageResponse<AdminFinancialDetailsResponse.SaleDetails> res = adminFinancialService.getSalesPage(1, 10, "2026-01-01", "2026-12-31");

        assertNotNull(res);
        assertEquals(0, res.getContent().size());
    }

    @Test
    void getPayoutsPage_Success() {
        UserEntity user = UserEntity.builder().displayname("Tutor").email("tutor@test.com").build();
        WalletEntity wallet = WalletEntity.builder().user(user).build();
        PayoutRequestEntity payout = PayoutRequestEntity.builder()
                .id(15)
                .wallet(wallet)
                .amount(BigDecimal.valueOf(1000000))
                .bankAccountNumber("123456789")
                .status(PayoutStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        Page<PayoutRequestEntity> pageObj = new PageImpl<>(List.of(payout));
        when(payoutRequestRepository.findAll(any(Pageable.class))).thenReturn(pageObj);

        PageResponse<AdminFinancialPayoutDetailsResponse> res = adminFinancialService.getPayoutsPage(1, 10, null, null);

        assertNotNull(res);
        assertEquals(1, res.getContent().size());
        assertEquals("Tutor", res.getContent().get(0).getInstructorName());
        assertEquals("****6789", res.getContent().get(0).getBankAccount());
    }
}
