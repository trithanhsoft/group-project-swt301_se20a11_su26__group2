package com.swp391.coding_platform.service.instructor;

import com.swp391.coding_platform.dto.response.*;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.course.EnrollmentEntity;
import com.swp391.coding_platform.entity.instructor.InstructorEntity;
import com.swp391.coding_platform.entity.payment.OrderItemEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.repository.course.CourseRepository;
import com.swp391.coding_platform.repository.course.EnrollmentRepository;
import com.swp391.coding_platform.repository.instructor.InstructorRepository;
import com.swp391.coding_platform.repository.payment.OrderItemRepository;
import com.swp391.coding_platform.repository.payment.PayoutRequestRepository;
import com.swp391.coding_platform.entity.payment.PayoutRequestEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstructorService {

    private final InstructorRepository instructorRepository;
    private final CourseRepository courseRepository;
    private final OrderItemRepository orderItemRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PayoutRequestRepository payoutRequestRepository;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;
            
    private static final DateTimeFormatter FRIENDLY_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
            .withZone(ZoneId.of("UTC"));


    public InstructorRevenueSummary getRevenueSummary(Integer userId, String filter, String startDate, String endDate) {
        InstructorEntity instructor = getInstructorByUserId(userId);
        List<OrderItemEntity> filteredItems = getFilteredOrderItems(instructor.getId(), filter, startDate, endDate);

        BigDecimal totalGross = BigDecimal.ZERO;
        for (OrderItemEntity item : filteredItems) {
            totalGross = totalGross.add(item.getPrice());
        }

        BigDecimal totalNet = totalGross.multiply(new BigDecimal("0.70"));

        // Group by month to calculate take-home with the 2M threshold
        java.util.Map<String, BigDecimal> monthlyGross = new java.util.HashMap<>();
        for (OrderItemEntity item : filteredItems) {
            java.time.Instant createdAt = item.getOrder().getCreatedAt();
            java.time.ZonedDateTime zdt = createdAt.atZone(ZoneId.of("UTC"));
            String monthKey = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            monthlyGross.put(monthKey, monthlyGross.getOrDefault(monthKey, BigDecimal.ZERO).add(item.getPrice()));
        }

        BigDecimal totalActualTakeHome = BigDecimal.ZERO;
        BigDecimal twoMillion = new BigDecimal("2000000");
        for (BigDecimal grossInMonth : monthlyGross.values()) {
            BigDecimal netInMonth = grossInMonth.multiply(new BigDecimal("0.70"));
            if (netInMonth.compareTo(twoMillion) > 0) {
                totalActualTakeHome = totalActualTakeHome.add(netInMonth.multiply(new BigDecimal("0.90")));
            } else {
                totalActualTakeHome = totalActualTakeHome.add(netInMonth);
            }
        }

        return new InstructorRevenueSummary(totalGross, totalNet, totalActualTakeHome);
    }

    public List<SalesHistoryItem> getSalesHistory(Integer userId, String filter, String startDate, String endDate) {
        InstructorEntity instructor = getInstructorByUserId(userId);
        List<OrderItemEntity> filteredItems = getFilteredOrderItems(instructor.getId(), filter, startDate, endDate);

        List<SalesHistoryItem> salesHistory = new ArrayList<>();
        for (OrderItemEntity item : filteredItems) {
            String timestamp = ISO_FORMATTER.format(item.getOrder().getCreatedAt());
            salesHistory.add(SalesHistoryItem.builder()
                    .id("TX-" + item.getId())
                    .studentName(item.getOrder().getUser().getDisplayname())
                    .courseId(String.valueOf(item.getCourse().getId()))
                    .courseTitle(item.getCourse().getTitle())
                    .amount(item.getPrice())
                    .timestamp(timestamp)
                    .build());
        }
        return salesHistory;
    }

    public List<RecentRegistration> getRecentRegistrations(Integer userId) {
        InstructorEntity instructor = getInstructorByUserId(userId);
        List<EnrollmentEntity> enrollments = enrollmentRepository.findEnrollmentsByInstructorId(instructor.getId());
        List<RecentRegistration> recentRegistrations = new ArrayList<>();

        for (EnrollmentEntity enrollment : enrollments) {
            String timeStr = FRIENDLY_FORMATTER.format(enrollment.getEnrolledAt());
            String priceStr = formatVndPrice(enrollment.getCourse().getPrice());

            recentRegistrations.add(RecentRegistration.builder()
                    .studentName(enrollment.getUser().getDisplayname())
                    .avatar(enrollment.getUser().getAvatarurl() != null ? enrollment.getUser().getAvatarurl() : 
                            "https://ui-avatars.com/api/?name=" + enrollment.getUser().getDisplayname().replace(" ", "+") + "&background=eef7ee&color=46A040&bold=true")
                    .course(enrollment.getCourse().getTitle())
                    .time(timeStr)
                    .amount(priceStr)
                    .build());
        }
        return recentRegistrations;
    }

    public List<PayoutHistoryItem> getPayoutHistory(Integer userId) {
        InstructorEntity instructor = getInstructorByUserId(userId);
        List<PayoutHistoryItem> payoutHistory = new ArrayList<>();
        if (instructor.getUser().getWallet() != null) {
            List<PayoutRequestEntity> payouts = payoutRequestRepository.findByWalletIdOrderByCreatedAtDesc(
                    instructor.getUser().getWallet().getId());
            for (PayoutRequestEntity payout : payouts) {
                payoutHistory.add(PayoutHistoryItem.builder()
                        .id("PO-" + payout.getId())
                        .payoutPeriod(payout.getPayoutPeriod())
                        .amount(payout.getAmount())
                        .bankName(payout.getBankName())
                        .bankAccountNumber(payout.getBankAccountNumber())
                        .status(payout.getStatus().name())
                        .transactionReference(payout.getTransactionReference())
                        .adminNote(payout.getAdminNote())
                        .build());
            }
        }
        return payoutHistory;
    }

    public List<CourseBreakdownItem> getCourseBreakdown(Integer userId, String filter, String startDate, String endDate) {
        InstructorEntity instructor = getInstructorByUserId(userId);
        List<OrderItemEntity> filteredItems = getFilteredOrderItems(instructor.getId(), filter, startDate, endDate);

        BigDecimal totalGross = BigDecimal.ZERO;
        for (OrderItemEntity item : filteredItems) {
            totalGross = totalGross.add(item.getPrice());
        }

        List<CourseBreakdownItem> courseBreakdown = new ArrayList<>();
        List<CourseEntity> courses = courseRepository.findByInstructorId(instructor.getId());
        java.util.Map<Long, BigDecimal> courseGross = new java.util.HashMap<>();
        for (CourseEntity course : courses) {
            courseGross.put(course.getId(), BigDecimal.ZERO);
        }

        for (OrderItemEntity item : filteredItems) {
            Long courseId = item.getCourse().getId();
            if (courseGross.containsKey(courseId)) {
                courseGross.put(courseId, courseGross.get(courseId).add(item.getPrice()));
            }
        }

        for (CourseEntity course : courses) {
            BigDecimal amount = courseGross.get(course.getId());
            int percentage = 0;
            if (totalGross.compareTo(BigDecimal.ZERO) > 0) {
                percentage = amount.multiply(new BigDecimal("100"))
                        .divide(totalGross, 0, java.math.RoundingMode.HALF_UP)
                        .intValue();
            }
            courseBreakdown.add(CourseBreakdownItem.builder()
                    .courseId(String.valueOf(course.getId()))
                    .courseTitle(course.getTitle())
                    .amount(amount)
                    .percentage(percentage)
                    .build());
        }
        courseBreakdown.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
        return courseBreakdown;
    }

    public List<MonthlyChartItem> getMonthlyChartData(Integer userId) {
        InstructorEntity instructor = getInstructorByUserId(userId);
        List<OrderItemEntity> orderItems = orderItemRepository.findCompletedItemsByInstructorId(instructor.getId());
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.of("UTC"))
                .withDayOfMonth(1)
                .truncatedTo(java.time.temporal.ChronoUnit.DAYS);

        List<MonthlyChartItem> monthlyChartData = new ArrayList<>();
        java.time.format.DateTimeFormatter labelFormatter = java.time.format.DateTimeFormatter.ofPattern("MM/yyyy", java.util.Locale.US);
        for (int i = 11; i >= 0; i--) {
            java.time.ZonedDateTime targetMonth = now.minusMonths(i);
            String label = targetMonth.format(labelFormatter);
            int yr = targetMonth.getYear();
            int mn = targetMonth.getMonthValue() - 1; // 0-indexed
            monthlyChartData.add(new MonthlyChartItem(label, yr, mn, BigDecimal.ZERO, 0));
        }

        for (OrderItemEntity item : orderItems) {
            java.time.Instant createdAt = item.getOrder().getCreatedAt();
            java.time.ZonedDateTime zdt = createdAt.atZone(java.time.ZoneId.of("UTC"));
            int yr = zdt.getYear();
            int mn = zdt.getMonthValue() - 1;
            
            for (MonthlyChartItem chartItem : monthlyChartData) {
                if (chartItem.getYear() == yr && chartItem.getMonth() == mn) {
                    chartItem.setAmount(chartItem.getAmount().add(item.getPrice()));
                    chartItem.setCount(chartItem.getCount() + 1);
                    break;
                }
            }
        }
        return monthlyChartData;
    }

    public InstructorCourseRegistrationsResponse getCourseRegistrations(Integer userId, String trendTimeframe) {
        InstructorEntity instructor = getInstructorByUserId(userId);
        List<OrderItemEntity> orderItems = orderItemRepository.findCompletedItemsByInstructorId(instructor.getId());
        List<CourseEntity> courses = courseRepository.findByInstructorId(instructor.getId());
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.of("UTC"))
                .withDayOfMonth(1)
                .truncatedTo(java.time.temporal.ChronoUnit.DAYS);

        int trendMonths = 12;
        if ("1m".equalsIgnoreCase(trendTimeframe)) trendMonths = 1;
        else if ("3m".equalsIgnoreCase(trendTimeframe)) trendMonths = 3;
        else if ("9m".equalsIgnoreCase(trendTimeframe)) trendMonths = 9;

        java.time.Instant trendCutoff = now.minusMonths(trendMonths).toInstant();
        
        List<CourseRegistrationsItem> courseRegistrations = new ArrayList<>();
        java.util.Map<Long, Integer> courseRegCounts = new java.util.HashMap<>();
        for (CourseEntity course : courses) {
            courseRegCounts.put(course.getId(), 0);
        }

        int totalTrendRegistrations = 0;
        for (OrderItemEntity item : orderItems) {
            java.time.Instant createdAt = item.getOrder().getCreatedAt();
            if (!createdAt.isBefore(trendCutoff)) {
                totalTrendRegistrations++;
                Long courseId = item.getCourse().getId();
                if (courseRegCounts.containsKey(courseId)) {
                    courseRegCounts.put(courseId, courseRegCounts.get(courseId) + 1);
                }
            }
        }

        for (CourseEntity course : courses) {
            courseRegistrations.add(CourseRegistrationsItem.builder()
                    .courseId(String.valueOf(course.getId()))
                    .courseTitle(course.getTitle())
                    .count(courseRegCounts.get(course.getId()))
                    .build());
        }
        courseRegistrations.sort((a, b) -> b.getCount().compareTo(a.getCount()));

        return InstructorCourseRegistrationsResponse.builder()
                .courseRegistrations(courseRegistrations)
                .totalTrendRegistrations(totalTrendRegistrations)
                .build();
    }

    private InstructorEntity getInstructorByUserId(Integer userId) {
        InstructorEntity instructor = instructorRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        if (instructor.getStatus() == com.swp391.coding_platform.entity.enums.InstructorStatus.SUSPENDED) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        return instructor;
    }

    private static class TimeRange {
        private final java.time.Instant start;
        private final java.time.Instant end;

        public TimeRange(java.time.Instant start, java.time.Instant end) {
            this.start = start;
            this.end = end;
        }

        public java.time.Instant start() {
            return start;
        }

        public java.time.Instant end() {
            return end;
        }
    }

    private TimeRange parseTimeRange(String filter, String startDate, String endDate, java.time.ZonedDateTime now) {
        java.time.Instant start = null;
        java.time.Instant end = null;

        if ("this-month".equalsIgnoreCase(filter)) {
            java.time.ZonedDateTime startOfThisMonth = now.withDayOfMonth(1).truncatedTo(java.time.temporal.ChronoUnit.DAYS);
            start = startOfThisMonth.toInstant();
            end = startOfThisMonth.plusMonths(1).toInstant();
        } else if ("last-month".equalsIgnoreCase(filter)) {
            java.time.ZonedDateTime startOfLastMonth = now.minusMonths(1).withDayOfMonth(1).truncatedTo(java.time.temporal.ChronoUnit.DAYS);
            start = startOfLastMonth.toInstant();
            end = startOfLastMonth.plusMonths(1).toInstant();
        } else if (filter != null && filter.startsWith("prev-")) {
            try {
                int diff = Integer.parseInt(filter.split("-")[1]);
                java.time.ZonedDateTime startOfPrevMonth = now.minusMonths(diff).withDayOfMonth(1).truncatedTo(java.time.temporal.ChronoUnit.DAYS);
                start = startOfPrevMonth.toInstant();
                end = startOfPrevMonth.plusMonths(1).toInstant();
            } catch (Exception e) {
                log.error("Error parsing prev- filter: {}", filter, e);
            }
        } else if ("custom".equalsIgnoreCase(filter)) {
            if (startDate != null && !startDate.isEmpty()) {
                try {
                    java.time.LocalDate localStart = java.time.LocalDate.parse(startDate);
                    start = localStart.atStartOfDay(java.time.ZoneId.of("UTC")).toInstant();
                } catch (Exception e) {
                    log.error("Error parsing startDate: {}", startDate, e);
                }
            }
            if (endDate != null && !endDate.isEmpty()) {
                try {
                    java.time.LocalDate localEnd = java.time.LocalDate.parse(endDate);
                    end = localEnd.plusDays(1).atStartOfDay(java.time.ZoneId.of("UTC")).toInstant();
                } catch (Exception e) {
                    log.error("Error parsing endDate: {}", endDate, e);
                }
            }
        }
        return new TimeRange(start, end);
    }

    private List<OrderItemEntity> getFilteredOrderItems(Integer instructorId, String filter, String startDate, String endDate) {
        List<OrderItemEntity> orderItems = orderItemRepository.findCompletedItemsByInstructorId(instructorId);
        TimeRange range = parseTimeRange(filter, startDate, endDate, java.time.ZonedDateTime.now(java.time.ZoneId.of("UTC")));
        List<OrderItemEntity> filtered = new ArrayList<>();
        for (OrderItemEntity item : orderItems) {
            java.time.Instant createdAt = item.getOrder().getCreatedAt();
            boolean matches = true;
            if (range.start() != null && createdAt.isBefore(range.start())) {
                matches = false;
            }
            if (range.end() != null && !createdAt.isBefore(range.end())) {
                matches = false;
            }
            if (matches) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private String formatVndPrice(BigDecimal price) {
        if (price == null) return "0 ₫";
        java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance(java.util.Locale.GERMANY); // formats using dots like 499.000
        return nf.format(price.longValue()) + " ₫";
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<AdminInstructorResponse> getAllInstructorsForAdmin() {
        List<InstructorEntity> instructors = instructorRepository.findAll();
        List<AdminInstructorResponse> responseList = new ArrayList<>();
        for (InstructorEntity inst : instructors) {
            List<CourseEntity> courses = courseRepository.findByInstructorId(inst.getId());
            int coursesCount = courses.size();
            double totalRating = 0.0;
            int studentsCount = 0;
            for (CourseEntity course : courses) {
                if (course.getAverageRating() != null) {
                    totalRating += course.getAverageRating();
                }
                if (course.getTotalEnrolled() != null) {
                    studentsCount += course.getTotalEnrolled();
                }
            }
            double averageRating = coursesCount > 0 ? (double) Math.round((totalRating / coursesCount) * 10) / 10 : 0.0;

            responseList.add(AdminInstructorResponse.builder()
                    .id(inst.getId())
                    .userId(inst.getUser() != null ? inst.getUser().getId() : null)
                    .fullName(inst.getFullName())
                    .major(inst.getMajor())
                    .bio(inst.getBio())
                    .status(inst.getStatus() != null ? inst.getStatus().name() : "ACTIVE")
                    .coursesCount(coursesCount)
                    .rating(averageRating)
                    .studentsCount(studentsCount)
                    .build());
        }
        return responseList;
    }

    @org.springframework.transaction.annotation.Transactional
    public AdminInstructorResponse updateInstructorStatus(Integer id, String statusStr) {
        InstructorEntity inst = instructorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        com.swp391.coding_platform.entity.enums.InstructorStatus newStatus;
        try {
            newStatus = com.swp391.coding_platform.entity.enums.InstructorStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + statusStr);
        }

        inst.setStatus(newStatus);
        inst = instructorRepository.save(inst);

        List<CourseEntity> courses = courseRepository.findByInstructorId(inst.getId());
        int coursesCount = courses.size();
        double totalRating = 0.0;
        int studentsCount = 0;
        for (CourseEntity course : courses) {
            if (course.getAverageRating() != null) {
                totalRating += course.getAverageRating();
            }
            if (course.getTotalEnrolled() != null) {
                studentsCount += course.getTotalEnrolled();
            }
        }
        double averageRating = coursesCount > 0 ? (double) Math.round((totalRating / coursesCount) * 10) / 10 : 0.0;

        return AdminInstructorResponse.builder()
                .id(inst.getId())
                .userId(inst.getUser() != null ? inst.getUser().getId() : null)
                .fullName(inst.getFullName())
                .major(inst.getMajor())
                .bio(inst.getBio())
                .status(inst.getStatus() != null ? inst.getStatus().name() : "ACTIVE")
                .coursesCount(coursesCount)
                .rating(averageRating)
                .studentsCount(studentsCount)
                .build();
    }
}

