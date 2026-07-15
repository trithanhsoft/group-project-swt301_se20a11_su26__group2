package com.swp391.coding_platform.service.admin;

import com.swp391.coding_platform.dto.response.AdminFinancialMonthlyRecordResponse;
import com.swp391.coding_platform.dto.response.AdminFinancialTopCourseResponse;
import com.swp391.coding_platform.dto.response.AdminFinancialDetailsResponse;
import com.swp391.coding_platform.dto.response.PageResponse;
import com.swp391.coding_platform.dto.response.AdminFinancialPayoutDetailsResponse;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.enums.OrderStatus;
import com.swp391.coding_platform.entity.enums.StatusTransaction;
import com.swp391.coding_platform.entity.enums.TransactionType;
import com.swp391.coding_platform.entity.payment.OrderEntity;
import com.swp391.coding_platform.entity.payment.OrderItemEntity;
import com.swp391.coding_platform.entity.payment.WalletTransactionEntity;
import com.swp391.coding_platform.repository.payment.OrderItemRepository;
import com.swp391.coding_platform.repository.payment.WalletTransactionRepository;
import com.swp391.coding_platform.repository.payment.OrderRepository;
import com.swp391.coding_platform.repository.payment.PayoutRequestRepository;
import com.swp391.coding_platform.entity.payment.PayoutRequestEntity;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class AdminFinancialService {

    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    WalletTransactionRepository walletTransactionRepository;
    PayoutRequestRepository payoutRequestRepository;

    @Transactional(readOnly = true)
    public List<AdminFinancialMonthlyRecordResponse> getMonthlyFinancialRecords() {
        // Query completed orders
        List<OrderEntity> completedOrders = orderRepository.findAllByStatusWithDetails(OrderStatus.COMPLETED);

        // Query successful AWARD transactions (if any)
        List<WalletTransactionEntity> awards = walletTransactionRepository.findAll().stream()
                .filter(t -> t.getType() == TransactionType.AWARD && t.getStatus() == StatusTransaction.SUCCESS)
                .collect(Collectors.toList());

        List<AdminFinancialMonthlyRecordResponse> records = new ArrayList<>();
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MMM yy", Locale.ENGLISH);
        DateTimeFormatter datePrefixFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate today = LocalDate.now();

        Instant oneYearAgo = today.minusMonths(11).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        for (int i = 11; i >= 0; i--) {
            LocalDate monthDate = today.minusMonths(i);
            String label = monthDate.format(labelFormatter);
            String datePrefix = monthDate.format(datePrefixFormatter);

            records.add(new AdminFinancialMonthlyRecordResponse(label, datePrefix, 0L, 0L, 0L, 0L, 0L));
        }

        Map<String, AdminFinancialMonthlyRecordResponse> recordMap = records.stream()
                .collect(Collectors.toMap(AdminFinancialMonthlyRecordResponse::getLabel, r -> r));

        // Aggregate completed orders for gross revenue and sales counts
        for (OrderEntity order : completedOrders) {
            if (order.getCreatedAt() != null && order.getCreatedAt().isAfter(oneYearAgo)) {
                LocalDate date = order.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
                String label = date.format(labelFormatter);
                AdminFinancialMonthlyRecordResponse rec = recordMap.get(label);
                if (rec != null) {
                    rec.setGross(rec.getGross() + order.getTotalAmount().longValue());
                    long itemsCount = order.getOrderItems() != null ? order.getOrderItems().size() : 0;
                    rec.setCount(rec.getCount() + itemsCount);
                }
            }
        }

        // Aggregate awards
        for (WalletTransactionEntity tx : awards) {
            if (tx.getCreatedAt() != null && tx.getCreatedAt().isAfter(oneYearAgo)) {
                LocalDate date = tx.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
                String label = date.format(labelFormatter);
                AdminFinancialMonthlyRecordResponse rec = recordMap.get(label);
                if (rec != null) {
                    rec.setRewards(rec.getRewards() + tx.getAmount().longValue());
                }
            }
        }

        return records;
    }

    @Transactional(readOnly = true)
    public List<AdminFinancialTopCourseResponse> getTopRevenueCoursesData() {
        List<OrderEntity> allCompletedOrders = orderRepository.findAllByStatusWithDetails(OrderStatus.COMPLETED);

        Map<CourseEntity, List<OrderItemEntity>> itemsByCourse = allCompletedOrders.stream()
                .filter(order -> order.getOrderItems() != null)
                .flatMap(order -> order.getOrderItems().stream())
                .filter(item -> item.getCourse() != null)
                .collect(Collectors.groupingBy(OrderItemEntity::getCourse));

        List<AdminFinancialTopCourseResponse> topRevenueCourses = itemsByCourse.entrySet().stream()
                .map(entry -> {
                    CourseEntity course = entry.getKey();
                    List<OrderItemEntity> items = entry.getValue();
                    long sold = items.size();
                    long gross = items.stream().mapToLong(item -> item.getPrice().longValue()).sum();
                    long payout = Math.round(gross * 0.7);
                    long plat = Math.round(gross * 0.3);
                    String tutor = course.getInstructor() != null ? course.getInstructor().getFullName() : "Unknown Instructor";
                    return new AdminFinancialTopCourseResponse(course.getTitle(), tutor, sold, gross, payout, plat);
                })
                .sorted((c1, c2) -> Long.compare(c2.getGross(), c1.getGross()))
                .limit(20)
                .collect(Collectors.toList());

        return topRevenueCourses;
    }

    @Transactional(readOnly = true)
    public AdminFinancialDetailsResponse getFinancialDetails() {
        // Query completed orders with details to avoid N+1 queries
        List<OrderEntity> completedOrders = orderRepository.findAllByStatusWithDetails(OrderStatus.COMPLETED);

        // Query successful AWARD transactions
        List<WalletTransactionEntity> awards = walletTransactionRepository.findAll().stream()
                .filter(t -> t.getType() == TransactionType.AWARD && t.getStatus() == StatusTransaction.SUCCESS)
                .collect(Collectors.toList());

        // 4. Monthly breakdowns for all time
        List<AdminFinancialDetailsResponse.MonthlyFinancialBreakdown> monthlyBreakdowns = 
                getMonthlyFinancialBreakdownsAllTime(completedOrders, awards);

        return AdminFinancialDetailsResponse.builder()
                .orders(new ArrayList<>())
                .awards(new ArrayList<>())
                .sales(new ArrayList<>())
                .monthlyBreakdowns(monthlyBreakdowns)
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminFinancialDetailsResponse.OrderDetails> getOrdersPage(int page, int size, String startDate, String endDate) {
        Pageable pageable = PageRequest.of(page - 1, size);
        
        Instant start = null;
        Instant end = null;
        try {
            if (startDate != null && !startDate.isEmpty()) {
                start = LocalDate.parse(startDate).atStartOfDay(ZoneId.systemDefault()).toInstant();
            }
            if (endDate != null && !endDate.isEmpty()) {
                end = LocalDate.parse(endDate).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusMillis(1);
            }
        } catch (Exception e) {
            log.warn("Invalid date format: startDate={}, endDate={}", startDate, endDate);
        }

        Page<OrderEntity> orderPage;
        if (start != null && end != null) {
            orderPage = orderRepository.findByStatusAndCreatedAtBetweenOrderByCreatedAtDesc(OrderStatus.COMPLETED, start, end, pageable);
        } else {
            orderPage = orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.COMPLETED, pageable);
        }
        
        List<AdminFinancialDetailsResponse.OrderDetails> content = orderPage.getContent().stream().map(o -> {
            String customerName = o.getUser() != null ? o.getUser().getDisplayname() : "Unknown";
            String customerEmail = o.getUser() != null ? o.getUser().getEmail() : "Unknown";
            String courses = o.getOrderItems() != null ? o.getOrderItems().stream()
                    .map(item -> item.getCourse() != null ? item.getCourse().getTitle() : "Unknown Course")
                    .collect(Collectors.joining(", ")) : "";
            long gross = o.getTotalAmount().longValue();
            long plat = Math.round(gross * 0.3);
            long instructor = Math.round(gross * 0.7);

            return AdminFinancialDetailsResponse.OrderDetails.builder()
                    .id(String.valueOf(o.getId()))
                    .customerName(customerName)
                    .customerEmail(customerEmail)
                    .courses(courses)
                    .grossAmount(gross)
                    .instructorShare(instructor)
                    .platformCut(plat)
                    .date(o.getCreatedAt().toString())
                    .build();
        }).collect(Collectors.toList());

        PageResponse<AdminFinancialDetailsResponse.OrderDetails> response = new PageResponse<>();
        response.setPage(orderPage.getNumber() + 1);
        response.setSize(orderPage.getSize());
        response.setTotalElements(orderPage.getTotalElements());
        response.setTotalPages(orderPage.getTotalPages());
        response.setContent(content);
        return response;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminFinancialDetailsResponse.AwardDetails> getAwardsPage(int page, int size, String startDate, String endDate) {
        Pageable pageable = PageRequest.of(page - 1, size);
        
        Instant start = null;
        Instant end = null;
        try {
            if (startDate != null && !startDate.isEmpty()) {
                start = LocalDate.parse(startDate).atStartOfDay(ZoneId.systemDefault()).toInstant();
            }
            if (endDate != null && !endDate.isEmpty()) {
                end = LocalDate.parse(endDate).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusMillis(1);
            }
        } catch (Exception e) {
            log.warn("Invalid date format: startDate={}, endDate={}", startDate, endDate);
        }

        Page<WalletTransactionEntity> awardPage;
        if (start != null && end != null) {
            awardPage = walletTransactionRepository.findByTypeAndStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
                    TransactionType.AWARD, StatusTransaction.SUCCESS, start, end, pageable);
        } else {
            awardPage = walletTransactionRepository.findByTypeAndStatusOrderByCreatedAtDesc(
                    TransactionType.AWARD, StatusTransaction.SUCCESS, pageable);
        }

        List<AdminFinancialDetailsResponse.AwardDetails> content = awardPage.getContent().stream().map(t -> {
            String userName = "Unknown";
            String userEmail = "Unknown";
            if (t.getWallet() != null && t.getWallet().getUser() != null) {
                userName = t.getWallet().getUser().getDisplayname();
                userEmail = t.getWallet().getUser().getEmail();
            }
            return AdminFinancialDetailsResponse.AwardDetails.builder()
                    .id(String.valueOf(t.getId()))
                    .userName(userName)
                    .userEmail(userEmail)
                    .amount(t.getAmount().longValue())
                    .date(t.getCreatedAt().toString())
                    .referenceId(t.getReferenceId())
                    .build();
        }).collect(Collectors.toList());

        PageResponse<AdminFinancialDetailsResponse.AwardDetails> response = new PageResponse<>();
        response.setPage(awardPage.getNumber() + 1);
        response.setSize(awardPage.getSize());
        response.setTotalElements(awardPage.getTotalElements());
        response.setTotalPages(awardPage.getTotalPages());
        response.setContent(content);
        return response;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminFinancialDetailsResponse.SaleDetails> getSalesPage(int page, int size, String startDate, String endDate) {
        Pageable pageable = PageRequest.of(page - 1, size);
        
        Instant start = null;
        Instant end = null;
        try {
            if (startDate != null && !startDate.isEmpty()) {
                start = LocalDate.parse(startDate).atStartOfDay(ZoneId.systemDefault()).toInstant();
            }
            if (endDate != null && !endDate.isEmpty()) {
                end = LocalDate.parse(endDate).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusMillis(1);
            }
        } catch (Exception e) {
            log.warn("Invalid date format: startDate={}, endDate={}", startDate, endDate);
        }

        Page<OrderItemEntity> salesPage;
        if (start != null && end != null) {
            salesPage = orderItemRepository.findByOrderStatusAndOrderCreatedAtBetweenOrderByOrderCreatedAtDesc(OrderStatus.COMPLETED, start, end, pageable);
        } else {
            salesPage = orderItemRepository.findByOrderStatusOrderByOrderCreatedAtDesc(OrderStatus.COMPLETED, pageable);
        }

        List<AdminFinancialDetailsResponse.SaleDetails> content = salesPage.getContent().stream().map(item -> {
            String courseTitle = item.getCourse() != null ? item.getCourse().getTitle() : "Unknown Course";
            String instructorName = (item.getCourse() != null && item.getCourse().getInstructor() != null) ? 
                    item.getCourse().getInstructor().getFullName() : "Unknown Instructor";
            String customerName = (item.getOrder() != null && item.getOrder().getUser() != null) ? 
                    item.getOrder().getUser().getDisplayname() : "Unknown Customer";

            return AdminFinancialDetailsResponse.SaleDetails.builder()
                    .orderId(String.valueOf(item.getOrder().getId()))
                    .courseTitle(courseTitle)
                    .instructorName(instructorName)
                    .customerName(customerName)
                    .price(item.getPrice().longValue())
                    .date(item.getOrder().getCreatedAt().toString())
                    .build();
        }).collect(Collectors.toList());

        PageResponse<AdminFinancialDetailsResponse.SaleDetails> response = new PageResponse<>();
        response.setPage(salesPage.getNumber() + 1);
        response.setSize(salesPage.getSize());
        response.setTotalElements(salesPage.getTotalElements());
        response.setTotalPages(salesPage.getTotalPages());
        response.setContent(content);
        return response;
    }

    private List<AdminFinancialDetailsResponse.MonthlyFinancialBreakdown> getMonthlyFinancialBreakdownsAllTime(
            List<OrderEntity> completedOrders, List<WalletTransactionEntity> awards) {

        List<AdminFinancialDetailsResponse.MonthlyFinancialBreakdown> records = new ArrayList<>();
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MMM yy", Locale.ENGLISH);
        DateTimeFormatter datePrefixFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate today = LocalDate.now();

        LocalDate startDate = today.minusMonths(11).withDayOfMonth(1);
        if (!completedOrders.isEmpty()) {
            LocalDate oldestOrderDate = completedOrders.stream()
                    .filter(o -> o.getCreatedAt() != null)
                    .map(o -> o.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate())
                    .min(Comparator.naturalOrder())
                    .orElse(today)
                    .withDayOfMonth(1);
            if (oldestOrderDate.isBefore(startDate)) {
                startDate = oldestOrderDate;
            }
        }

        LocalDate temp = startDate;
        while (!temp.isAfter(today)) {
            String label = temp.format(labelFormatter);
            String datePrefix = temp.format(datePrefixFormatter);

            records.add(new AdminFinancialDetailsResponse.MonthlyFinancialBreakdown(
                    label, datePrefix, 0L, 0L, 0L, 0L, 0L, 0L));
            temp = temp.plusMonths(1);
        }

        Map<String, AdminFinancialDetailsResponse.MonthlyFinancialBreakdown> recordMap = records.stream()
                .collect(Collectors.toMap(AdminFinancialDetailsResponse.MonthlyFinancialBreakdown::getLabel, r -> r));

        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

        for (OrderEntity order : completedOrders) {
            if (order.getCreatedAt() != null && !order.getCreatedAt().isBefore(startInstant)) {
                LocalDate date = order.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
                String label = date.format(labelFormatter);
                AdminFinancialDetailsResponse.MonthlyFinancialBreakdown rec = recordMap.get(label);
                if (rec != null) {
                    rec.setGross(rec.getGross() + order.getTotalAmount().longValue());
                    long itemsCount = order.getOrderItems() != null ? order.getOrderItems().size() : 0;
                    rec.setCount(rec.getCount() + itemsCount);
                }
            }
        }

        for (WalletTransactionEntity tx : awards) {
            if (tx.getCreatedAt() != null && !tx.getCreatedAt().isBefore(startInstant)) {
                LocalDate date = tx.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
                String label = date.format(labelFormatter);
                AdminFinancialDetailsResponse.MonthlyFinancialBreakdown rec = recordMap.get(label);
                if (rec != null) {
                    rec.setRewards(rec.getRewards() + tx.getAmount().longValue());
                }
            }
        }

        for (AdminFinancialDetailsResponse.MonthlyFinancialBreakdown rec : records) {
            long gross = rec.getGross();
            long platformShare = Math.round(gross * 0.3);
            long gatewayFees = Math.round(gross * 0.02);
            long otherExpenses = rec.getServer() + rec.getMarketing() + gatewayFees;
            long netProfit = platformShare - rec.getRewards() - otherExpenses;
            rec.setNetProfit(netProfit);
        }

        return records;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminFinancialPayoutDetailsResponse> getPayoutsPage(int page, int limit, String startDate, String endDate) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<PayoutRequestEntity> payoutPage = payoutRequestRepository.findAll(pageable);
        
        List<AdminFinancialPayoutDetailsResponse> payoutDetails = payoutPage.getContent().stream().map(p -> {
            String instructorName = p.getWallet() != null && p.getWallet().getUser() != null ? p.getWallet().getUser().getDisplayname() : "Unknown";
            String instructorEmail = p.getWallet() != null && p.getWallet().getUser() != null ? p.getWallet().getUser().getEmail() : "Unknown";
            return AdminFinancialPayoutDetailsResponse.builder()
                    .id("PO-" + p.getId())
                    .instructorName(instructorName)
                    .instructorEmail(instructorEmail)
                    .amount(p.getAmount())
                    .bankAccount(p.getBankAccountNumber() != null ? "****" + (p.getBankAccountNumber().length() >= 4 ? p.getBankAccountNumber().substring(p.getBankAccountNumber().length() - 4) : p.getBankAccountNumber()) : "N/A")
                    .status(p.getStatus().name())
                    .date(p.getCreatedAt() != null ? p.getCreatedAt() : Instant.now())
                    .build();
        }).collect(Collectors.toList());

        return PageResponse.<AdminFinancialPayoutDetailsResponse>builder()
                .content(payoutDetails)
                .page(payoutPage.getNumber() + 1)
                .size(payoutPage.getSize())
                .totalElements(payoutPage.getTotalElements())
                .totalPages(payoutPage.getTotalPages())
                .numberOfElements(payoutPage.getNumberOfElements())
                .first(payoutPage.isFirst())
                .last(payoutPage.isLast())
                .build();
    }
}
