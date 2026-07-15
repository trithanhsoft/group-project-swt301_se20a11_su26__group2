package com.swp391.coding_platform.service.admin;

import com.swp391.coding_platform.dto.response.*;
import com.swp391.coding_platform.dto.response.AdminDashboardStatsResponse.MonthlyStat;
import com.swp391.coding_platform.dto.response.AdminDashboardStatsResponse.TopCategory;
import com.swp391.coding_platform.dto.response.AdminDashboardStatsResponse.TopCourse;
import com.swp391.coding_platform.dto.response.AdminDashboardStatsResponse.TopInstructor;
import com.swp391.coding_platform.dto.response.AdminDashboardStatsResponse.TopProblem;
import com.swp391.coding_platform.entity.enums.OrderStatus;
import com.swp391.coding_platform.entity.enums.StatusTransaction;
import com.swp391.coding_platform.entity.enums.TransactionType;
import com.swp391.coding_platform.entity.enums.UserStatus;
import com.swp391.coding_platform.entity.payment.OrderEntity;
import com.swp391.coding_platform.entity.payment.WalletEntity;
import com.swp391.coding_platform.entity.payment.WalletTransactionEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.repository.category.CategoryRepository;
import com.swp391.coding_platform.repository.contest.ContestRepository;
import com.swp391.coding_platform.repository.course.CourseRepository;
import com.swp391.coding_platform.repository.instructor.InstructorRepository;
import com.swp391.coding_platform.repository.payment.OrderRepository;
import com.swp391.coding_platform.repository.payment.WalletTransactionRepository;
import com.swp391.coding_platform.repository.problem.ProblemRepository;
import com.swp391.coding_platform.repository.problem.ProblemSubmissionRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ContestRepository contestRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private InstructorRepository instructorRepository;
    @Mock
    private ProblemRepository problemRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private WalletTransactionRepository walletTransactionRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProblemSubmissionRepository problemSubmissionRepository;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @Test
    void getDashboardStats_Success() {
        when(userRepository.countByStatus(UserStatus.ACTIVE)).thenReturn(100L);
        when(contestRepository.countActiveContests(any())).thenReturn(5L);
        when(courseRepository.count()).thenReturn(50L);
        when(instructorRepository.count()).thenReturn(20L);
        when(problemRepository.count()).thenReturn(200L);
        
        when(orderRepository.findAllByStatus(any())).thenReturn(Collections.emptyList());
        when(userRepository.findAllByCreatedAtAfter(any())).thenReturn(Collections.emptyList());
        when(categoryRepository.findCategoryEnrollmentCounts()).thenReturn(Collections.emptyList());
        when(courseRepository.findTopCoursesDynamic(any())).thenReturn(Collections.emptyList());
        when(courseRepository.findTopInstructors(any())).thenReturn(Collections.emptyList());
        when(problemSubmissionRepository.findTopProblems(any())).thenReturn(Collections.emptyList());

        AdminDashboardStatsResponse response = adminDashboardService.getDashboardStats();

        assertNotNull(response);
        assertEquals(100L, response.getActiveUsers());
        assertEquals(5L, response.getActiveContests());
        assertEquals(50L, response.getTotalCourses());
        assertEquals(20L, response.getTotalInstructors());
        assertEquals(200L, response.getTotalProblems());
        assertEquals(0L, response.getTotalRevenue());
    }

    @Test
    void getRecentDeposits_withVaryingWalletAndUserStatus() {
        WalletTransactionEntity tx1 = new WalletTransactionEntity();
        tx1.setId(1);
        tx1.setAmount(BigDecimal.valueOf(1000));
        tx1.setCreatedAt(Instant.now());
        tx1.setWallet(null); // Case 1: Null wallet

        WalletTransactionEntity tx2 = new WalletTransactionEntity();
        tx2.setId(2);
        tx2.setAmount(BigDecimal.valueOf(2000));
        tx2.setCreatedAt(Instant.now());
        WalletEntity w2 = new WalletEntity();
        w2.setUser(null); // Case 2: Non-null wallet, null user
        tx2.setWallet(w2);

        WalletTransactionEntity tx3 = new WalletTransactionEntity();
        tx3.setId(3);
        tx3.setAmount(BigDecimal.valueOf(3000));
        tx3.setCreatedAt(Instant.now());
        WalletEntity w3 = new WalletEntity();
        UserEntity u3 = UserEntity.builder().displayname("Alice").build();
        w3.setUser(u3); // Case 3: Both wallet and user non-null
        tx3.setWallet(w3);

        when(walletTransactionRepository.findRecentTransactions(eq(TransactionType.DEPOSIT), eq(StatusTransaction.SUCCESS), any(Pageable.class)))
                .thenReturn(List.of(tx1, tx2, tx3));

        List<AdminDepositHistoryResponse> result = adminDashboardService.getRecentDeposits();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Unknown User", result.get(0).getUserName());
        assertEquals("Unknown User", result.get(1).getUserName());
        assertEquals("Alice", result.get(2).getUserName());
        assertEquals(1000L, result.get(0).getAmount());
        assertEquals(2000L, result.get(1).getAmount());
        assertEquals(3000L, result.get(2).getAmount());
    }

    @Test
    void getAllDeposits_Success() {
        WalletTransactionEntity tx = new WalletTransactionEntity();
        tx.setId(1);
        tx.setAmount(BigDecimal.valueOf(5000));
        tx.setCreatedAt(Instant.now());
        
        when(walletTransactionRepository.findRecentTransactions(eq(TransactionType.DEPOSIT), eq(StatusTransaction.SUCCESS), any(Pageable.class)))
                .thenReturn(List.of(tx));

        List<AdminDepositHistoryResponse> result = adminDashboardService.getAllDeposits();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(5000L, result.get(0).getAmount());
    }

    @Test
    void getDashboardStats_withMonthlyAggregations() {
        when(userRepository.countByStatus(UserStatus.ACTIVE)).thenReturn(10L);
        when(contestRepository.countActiveContests(any())).thenReturn(1L);
        
        // Orders setup
        OrderEntity order1 = new OrderEntity();
        order1.setStatus(OrderStatus.COMPLETED);
        order1.setTotalAmount(BigDecimal.valueOf(500L));
        order1.setCreatedAt(Instant.now().minus(5, ChronoUnit.DAYS)); // Valid order

        OrderEntity order2 = new OrderEntity();
        order2.setStatus(OrderStatus.COMPLETED);
        order2.setTotalAmount(BigDecimal.valueOf(1000L));
        order2.setCreatedAt(null); // Case: createdAt null

        OrderEntity order3 = new OrderEntity();
        order3.setStatus(OrderStatus.COMPLETED);
        order3.setTotalAmount(BigDecimal.valueOf(10000L));
        order3.setCreatedAt(Instant.now().minus(400, ChronoUnit.DAYS)); // Out of 12 month range

        when(orderRepository.findAllByStatus(OrderStatus.COMPLETED)).thenReturn(List.of(order1, order2, order3));

        // Users setup
        UserEntity u1 = UserEntity.builder().createdAt(Instant.now().minus(10, ChronoUnit.DAYS)).build(); // Valid user
        UserEntity u2 = UserEntity.builder().createdAt(null).build(); // Case: createdAt null
        UserEntity u3 = UserEntity.builder().createdAt(Instant.now().minus(400, ChronoUnit.DAYS)).build(); // Out of range

        when(userRepository.findAllByCreatedAtAfter(any(Instant.class))).thenReturn(List.of(u1, u2, u3));
        
        when(categoryRepository.findCategoryEnrollmentCounts()).thenReturn(Collections.emptyList());
        when(courseRepository.findTopCoursesDynamic(any())).thenReturn(Collections.emptyList());
        when(courseRepository.findTopInstructors(any())).thenReturn(Collections.emptyList());
        when(problemSubmissionRepository.findTopProblems(any())).thenReturn(Collections.emptyList());

        AdminDashboardStatsResponse response = adminDashboardService.getDashboardStats();

        assertNotNull(response);
        assertEquals(11500L, response.getTotalRevenue()); // 500 + 1000 + 10000
        
        List<MonthlyStat> chart = response.getFinancialChartData();
        assertNotNull(chart);
        assertEquals(12, chart.size());
        
        // The last month in chart should hold aggregate of u1 and order1
        MonthlyStat lastMonth = chart.get(11);
        assertEquals(1L, lastMonth.getUsersCount());
        assertEquals(500L, lastMonth.getAmount());
        assertEquals(1L, lastMonth.getCount());
    }

    @Test
    void getDashboardStats_getCategoryColorsAndSortingLimit() {
        Object[] cat1 = new Object[]{"Web Development", 100L};      // #F36F21
        Object[] cat2 = new Object[]{"Data Science", 200L};       // #12284C
        Object[] cat3 = new Object[]{"Mobile Applications", 50L}; // #10B981
        Object[] cat4 = new Object[]{"Cloud Computing", 150L};     // #3B82F6
        Object[] cat5 = new Object[]{"General Design", 10L};       // #6B7280
        Object[] cat6 = new Object[]{"Other Extra", 5L};           // Out of limit (truncation check)
        Object[] cat7 = new Object[]{null, 1L};                    // Case: Name null

        when(categoryRepository.findCategoryEnrollmentCounts()).thenReturn(List.of(cat1, cat2, cat3, cat4, cat5, cat6, cat7));
        
        when(orderRepository.findAllByStatus(any())).thenReturn(Collections.emptyList());
        when(userRepository.findAllByCreatedAtAfter(any())).thenReturn(Collections.emptyList());
        when(courseRepository.findTopCoursesDynamic(any())).thenReturn(Collections.emptyList());
        when(courseRepository.findTopInstructors(any())).thenReturn(Collections.emptyList());
        when(problemSubmissionRepository.findTopProblems(any())).thenReturn(Collections.emptyList());

        AdminDashboardStatsResponse response = adminDashboardService.getDashboardStats();

        assertNotNull(response);
        List<TopCategory> categories = response.getTopCategories();
        assertNotNull(categories);
        assertEquals(5, categories.size()); // Limited to top 5
        
        // Sorted descending: Data Science (200) -> Cloud Computing (150) -> Web Dev (100) -> Mobile (50) -> General (10)
        assertEquals("Data Science", categories.get(0).getName());
        assertEquals("#12284C", categories.get(0).getColor());
        
        assertEquals("Cloud Computing", categories.get(1).getName());
        assertEquals("#3B82F6", categories.get(1).getColor());
        
        assertEquals("Web Development", categories.get(2).getName());
        assertEquals("#F36F21", categories.get(2).getColor());
        
        assertEquals("Mobile Applications", categories.get(3).getName());
        assertEquals("#10B981", categories.get(3).getColor());
        
        assertEquals("General Design", categories.get(4).getName());
        assertEquals("#6B7280", categories.get(4).getColor());
    }

    @Test
    void getDashboardStats_getTopCoursesWithFallbackInstructor() {
        Object[] course1 = new Object[]{"Intro to Java", "Bob", 40L};
        Object[] course2 = new Object[]{"Intro to Python", null, 30L}; // Case: instructor name null

        when(courseRepository.findTopCoursesDynamic(any(Pageable.class))).thenReturn(List.of(course1, course2));

        when(orderRepository.findAllByStatus(any())).thenReturn(Collections.emptyList());
        when(userRepository.findAllByCreatedAtAfter(any())).thenReturn(Collections.emptyList());
        when(categoryRepository.findCategoryEnrollmentCounts()).thenReturn(Collections.emptyList());
        when(courseRepository.findTopInstructors(any())).thenReturn(Collections.emptyList());
        when(problemSubmissionRepository.findTopProblems(any())).thenReturn(Collections.emptyList());

        AdminDashboardStatsResponse response = adminDashboardService.getDashboardStats();

        assertNotNull(response);
        List<TopCourse> courses = response.getTopCourses();
        assertNotNull(courses);
        assertEquals(2, courses.size());
        assertEquals("Intro to Java", courses.get(0).getName());
        assertEquals("Bob", courses.get(0).getInstructor());
        
        assertEquals("Intro to Python", courses.get(1).getName());
        assertEquals("Unknown Instructor", courses.get(1).getInstructor());
    }
}
