package com.swp391.coding_platform.service.admin;

import com.swp391.coding_platform.dto.response.AdminDashboardStatsResponse;
import com.swp391.coding_platform.dto.response.AdminDashboardStatsResponse.MonthlyStat;
import com.swp391.coding_platform.dto.response.AdminDashboardStatsResponse.TopCategory;
import com.swp391.coding_platform.dto.response.AdminDashboardStatsResponse.TopCourse;
import com.swp391.coding_platform.dto.response.AdminDashboardStatsResponse.TopInstructor;
import com.swp391.coding_platform.dto.response.AdminDashboardStatsResponse.TopProblem;
import com.swp391.coding_platform.dto.response.AdminDepositHistoryResponse;

import com.swp391.coding_platform.entity.contest.ContestEntity;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.enums.OrderStatus;
import com.swp391.coding_platform.entity.enums.StatusTransaction;
import com.swp391.coding_platform.entity.enums.TransactionType;
import com.swp391.coding_platform.entity.enums.UserStatus;
import com.swp391.coding_platform.entity.payment.OrderEntity;
import com.swp391.coding_platform.entity.payment.WalletTransactionEntity;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.entity.instructor.InstructorEntity;
import com.swp391.coding_platform.repository.category.CategoryRepository;
import com.swp391.coding_platform.repository.contest.ContestRepository;
import com.swp391.coding_platform.repository.course.CourseRepository;
import com.swp391.coding_platform.repository.instructor.InstructorRepository;
import com.swp391.coding_platform.repository.payment.OrderRepository;
import com.swp391.coding_platform.repository.payment.WalletTransactionRepository;
import com.swp391.coding_platform.repository.problem.ProblemRepository;
import com.swp391.coding_platform.repository.problem.ProblemSubmissionRepository;
import com.swp391.coding_platform.repository.user.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminDashboardService {

    UserRepository userRepository;
    ContestRepository contestRepository;
    CourseRepository courseRepository;
    InstructorRepository instructorRepository;
    ProblemRepository problemRepository;
    OrderRepository orderRepository;
    WalletTransactionRepository walletTransactionRepository;
    CategoryRepository categoryRepository;
    ProblemSubmissionRepository problemSubmissionRepository;

    public AdminDashboardStatsResponse getDashboardStats() {
        // 1. KPI Counts
        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        long activeContests = contestRepository.countActiveContests(Instant.now());
        long totalCourses = courseRepository.count();
        long totalInstructors = instructorRepository.count();
        long totalProblems = problemRepository.count();

        // Total Revenue: sum of completed orders
        List<OrderEntity> allCompletedOrders = orderRepository.findAllByStatus(OrderStatus.COMPLETED);
        long totalRevenue = allCompletedOrders.stream()
                .mapToLong(o -> o.getTotalAmount().longValue())
                .sum();

        // 2. Platform Monthly Stats (Last 12 Months)
        List<MonthlyStat> financialChartData = getMonthlyStats(allCompletedOrders);

        // 3. Top Registered Categories
        List<TopCategory> topCategories = getTopCategories();

        // 4. Top Subscribed Courses
        List<TopCourse> topCourses = getTopSubscribedCourses();

        // 5. Top Instructors
        List<TopInstructor> topInstructors = getTopInstructors();

        // 6. Top Submitted Problems
        List<TopProblem> topProblems = getTopSubmittedProblems();

        return AdminDashboardStatsResponse.builder()
                .activeUsers(activeUsers)
                .activeContests(activeContests)
                .totalCourses(totalCourses)
                .totalInstructors(totalInstructors)
                .totalProblems(totalProblems)
                .totalRevenue(totalRevenue)
                .financialChartData(financialChartData)
                .topCategories(topCategories)
                .topCourses(topCourses)
                .topInstructors(topInstructors)
                .topProblems(topProblems)
                .build();
    }

    public List<AdminDepositHistoryResponse> getRecentDeposits() {
        Pageable pageable = PageRequest.of(0, 5);
        List<WalletTransactionEntity> deposits = walletTransactionRepository.findRecentTransactions(
                TransactionType.DEPOSIT, StatusTransaction.SUCCESS, pageable);

        return deposits.stream().map(wt -> {
            String userName = "Unknown User";
            if (wt.getWallet() != null && wt.getWallet().getUser() != null) {
                userName = wt.getWallet().getUser().getDisplayname();
            }
            return AdminDepositHistoryResponse.builder()
                    .id(String.valueOf(wt.getId()))
                    .userName(userName)
                    .amount(wt.getAmount().longValue())
                    .date(wt.getCreatedAt().toString())
                    .build();
        }).collect(Collectors.toList());
    }

    public List<AdminDepositHistoryResponse> getAllDeposits() {
        Pageable pageable = Pageable.unpaged();
        List<WalletTransactionEntity> deposits = walletTransactionRepository.findRecentTransactions(
                TransactionType.DEPOSIT, StatusTransaction.SUCCESS, pageable);

        return deposits.stream().map(wt -> {
            String userName = "Unknown User";
            if (wt.getWallet() != null && wt.getWallet().getUser() != null) {
                userName = wt.getWallet().getUser().getDisplayname();
            }
            return AdminDepositHistoryResponse.builder()
                    .id(String.valueOf(wt.getId()))
                    .userName(userName)
                    .amount(wt.getAmount().longValue())
                    .date(wt.getCreatedAt().toString())
                    .build();
        }).collect(Collectors.toList());
    }

    private List<MonthlyStat> getMonthlyStats(List<OrderEntity> completedOrders) {
        List<MonthlyStat> monthlyStats = new ArrayList<>();
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MMM yy", Locale.ENGLISH);
        LocalDate today = LocalDate.now();

        // Initialize 12 month buckets ending at the current month
        for (int i = 11; i >= 0; i--) {
            LocalDate monthDate = today.minusMonths(i);
            String label = monthDate.format(labelFormatter);
            monthlyStats.add(new MonthlyStat(label, 0L, 0L, 0L));
        }

        Map<String, MonthlyStat> statMap = monthlyStats.stream()
                .collect(Collectors.toMap(MonthlyStat::getLabel, s -> s));

        // Aggregate User Registrations
        Instant oneYearAgo = today.minusMonths(11).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        List<UserEntity> newUsers = userRepository.findAllByCreatedAtAfter(oneYearAgo);
        for (UserEntity user : newUsers) {
            if (user.getCreatedAt() != null) {
                LocalDate date = user.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
                String label = date.format(labelFormatter);
                MonthlyStat stat = statMap.get(label);
                if (stat != null) {
                    stat.setUsersCount(stat.getUsersCount() + 1);
                }
            }
        }

        // Aggregate Revenue and Course Sales (Completed Orders)
        for (OrderEntity order : completedOrders) {
            if (order.getCreatedAt() != null && order.getCreatedAt().isAfter(oneYearAgo)) {
                LocalDate date = order.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
                String label = date.format(labelFormatter);
                MonthlyStat stat = statMap.get(label);
                if (stat != null) {
                    stat.setAmount(stat.getAmount() + order.getTotalAmount().longValue());
                    stat.setCount(stat.getCount() + 1);
                }
            }
        }

        return monthlyStats;
    }

    private List<TopCategory> getTopCategories() {
        List<Object[]> results = categoryRepository.findCategoryEnrollmentCounts();
        List<TopCategory> topCategories = new ArrayList<>();

        String[] defaultColors = {"#F36F21", "#12284C", "#10B981", "#3B82F6", "#6B7280"};
        int colorIdx = 0;

        for (Object[] row : results) {
            String name = (String) row[0];
            long count = ((Number) row[1]).longValue();
            String color = getCategoryColor(name);
            topCategories.add(new TopCategory(name, count, color));
        }

        // Sort descending
        topCategories.sort((c1, c2) -> Long.compare(c2.getCount(), c1.getCount()));

        return topCategories.stream().limit(5).collect(Collectors.toList());
    }

    private String getCategoryColor(String name) {
        if (name == null) return "#6B7280";
        String lower = name.toLowerCase();
        if (lower.contains("web")) return "#F36F21";
        if (lower.contains("data") || lower.contains("ai") || lower.contains("science") || lower.contains("machine")) return "#12284C";
        if (lower.contains("mobile") || lower.contains("app")) return "#10B981";
        if (lower.contains("cloud") || lower.contains("devops")) return "#3B82F6";
        return "#6B7280";
    }

    private List<TopCourse> getTopSubscribedCourses() {
        Pageable pageable = PageRequest.of(0, 4);
        List<Object[]> results = courseRepository.findTopCoursesDynamic(pageable);
        List<TopCourse> topCourses = new ArrayList<>();

        String[] colors = {"#F36F21", "#10B981", "#3B82F6", "#6366F1"};
        int i = 0;

        for (Object[] row : results) {
            String title = (String) row[0];
            String instructorName = row[1] != null ? (String) row[1] : "Unknown Instructor";
            long count = ((Number) row[2]).longValue();
            String color = colors[i % colors.length];
            topCourses.add(new TopCourse(title, instructorName, count, color));
            i++;
        }

        return topCourses;
    }

    private List<TopInstructor> getTopInstructors() {
        Pageable pageable = PageRequest.of(0, 4);
        List<Object[]> results = courseRepository.findTopInstructors(pageable);
        List<TopInstructor> topInstructors = new ArrayList<>();

        String[] colors = {"#F36F21", "#12284C", "#10B981", "#3B82F6"};
        int i = 0;

        for (Object[] row : results) {
            String name = (String) row[0];
            long count = ((Number) row[1]).longValue();
            String color = colors[i % colors.length];
            topInstructors.add(new TopInstructor(name, count, color));
            i++;
        }

        return topInstructors;
    }

    private List<TopProblem> getTopSubmittedProblems() {
        Pageable pageable = PageRequest.of(0, 4);
        List<Object[]> results = problemSubmissionRepository.findTopProblems(pageable);
        List<TopProblem> topProblems = new ArrayList<>();

        String[] colors = {"#F36F21", "#12284C", "#10B981", "#3B82F6"};
        int i = 0;

        for (Object[] row : results) {
            String name = (String) row[0];
            String difficulty = String.valueOf(row[1]);
            long count = ((Number) row[2]).longValue();
            String color = colors[i % colors.length];
            topProblems.add(new TopProblem(name, difficulty, count, color));
            i++;
        }

        return topProblems;
    }

}
