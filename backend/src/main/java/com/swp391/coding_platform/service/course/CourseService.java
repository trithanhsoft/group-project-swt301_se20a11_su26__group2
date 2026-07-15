package com.swp391.coding_platform.service.course;

import com.swp391.coding_platform.dto.request.CourseSearchRequest;
import com.swp391.coding_platform.dto.response.CourseListItemResponse;
import com.swp391.coding_platform.dto.response.CourseDetailResponse;
import com.swp391.coding_platform.dto.response.CurriculumChapterResponse;
import com.swp391.coding_platform.dto.response.PageResponse;
import com.swp391.coding_platform.dto.response.LearningDetailResponse;
import com.swp391.coding_platform.dto.response.LearningLessonResponse;
import com.swp391.coding_platform.dto.response.LearningCurriculumChapterResponse;
import com.swp391.coding_platform.entity.course.LessonEntity;
import com.swp391.coding_platform.entity.course.EnrollmentEntity;
import com.swp391.coding_platform.entity.course.ChapterEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.mapper.CourseMapper;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.course.ChapterEntity;
import com.swp391.coding_platform.entity.progress.CompletedLessonsCountEntity;
import com.swp391.coding_platform.entity.progress.LessonProgressEntity;
import com.swp391.coding_platform.entity.enums.EnrollmentStatus;
import com.swp391.coding_platform.repository.course.CourseRepository;
import com.swp391.coding_platform.repository.course.ChapterRepository;
import com.swp391.coding_platform.repository.course.EnrollmentRepository;
import com.swp391.coding_platform.repository.progress.CompletedLessonCountRepository;
import com.swp391.coding_platform.repository.progress.LessonProgressRepository;
import com.swp391.coding_platform.repository.course.CourseReviewRepository;
import com.swp391.coding_platform.repository.specification.CourseSpecification;
import com.swp391.coding_platform.util.ProgressUtils;
import com.swp391.coding_platform.dto.response.CourseReviewDto;
import com.swp391.coding_platform.dto.response.CourseReviewStatsResponse;
import com.swp391.coding_platform.dto.request.CourseReviewRequest;
import com.swp391.coding_platform.entity.course.CourseReviewEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.repository.user.UserRepository;
import com.swp391.coding_platform.dto.request.CreateCommentRequest;
import com.swp391.coding_platform.dto.response.LessonCommentResponse;
import com.swp391.coding_platform.entity.course.LessonCommentEntity;
import com.swp391.coding_platform.repository.course.LessonCommentRepository;
import com.swp391.coding_platform.repository.course.LessonRepository;
import com.swp391.coding_platform.repository.course.LessonProblemRepository;
import com.swp391.coding_platform.repository.course.QuizRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CourseService {
    CourseRepository courseRepository;
    CourseMapper courseMapper;
    CompletedLessonCountRepository completedLessonCountRepository;
    LessonProgressRepository lessonProgressRepository;
    EnrollmentRepository enrollmentRepository;
    ChapterRepository chapterRepository;
    CourseReviewRepository courseReviewRepository;
    UserRepository userRepository;
    LessonCommentRepository lessonCommentRepository;
    LessonRepository lessonRepository;
    LessonProblemRepository lessonProblemRepository;
    QuizRepository quizRepository;
    QuizService quizService;

    public CourseService(
            CourseRepository courseRepository,
            CourseMapper courseMapper,
            CompletedLessonCountRepository completedLessonCountRepository,
            LessonProgressRepository lessonProgressRepository,
            EnrollmentRepository enrollmentRepository,
            ChapterRepository chapterRepository,
            CourseReviewRepository courseReviewRepository,
            UserRepository userRepository,
            LessonCommentRepository lessonCommentRepository,
            LessonRepository lessonRepository,
            LessonProblemRepository lessonProblemRepository,
            QuizRepository quizRepository,
            QuizService quizService
    ) {
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
        this.completedLessonCountRepository = completedLessonCountRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.chapterRepository = chapterRepository;
        this.courseReviewRepository = courseReviewRepository;
        this.userRepository = userRepository;
        this.lessonCommentRepository = lessonCommentRepository;
        this.lessonRepository = lessonRepository;
        this.lessonProblemRepository = lessonProblemRepository;
        this.quizRepository = quizRepository;
        this.quizService = quizService;
    }

    public PageResponse<CourseListItemResponse> getCourseList(Long userId, CourseSearchRequest searchRequest, Pageable pageable) {

        // 1. Khởi tạo Specification cơ bản (Luôn là khóa học ACTIVE)
        Specification<CourseEntity> spec = Specification.allOf(CourseSpecification.isStatusActive());

        // 2. Nối (Chaining) các điều kiện linh hoạt dựa vào Request từ User
        if (searchRequest != null) {
            spec = spec.and(CourseSpecification.hasKeyword(searchRequest.getKeyword()))
                    .and(CourseSpecification.hasCategories(searchRequest.getCategoryIds()))
                    .and(CourseSpecification.hasPriceBetween(searchRequest.getMinPrice(), searchRequest.getMaxPrice()))
                    .and(CourseSpecification.hasRatingBetween(searchRequest.getMinRating(), searchRequest.getMaxRating()))
                    .and(CourseSpecification.hasTeacherName(searchRequest.getInstructorName()));
        }

        // 3. Gọi DB (JpaSpecificationExecutor lo toàn bộ việc sinh câu SQL) (QUERY 1)
        Page<CourseEntity> courseEntities = courseRepository.findAll(spec, pageable);

        Set<Long> enrolledCourseIds = new HashSet<>();  // Lưu danh sách courseId mà user đã enrolled
        Map<Long, Integer> courseProgressMap = new HashMap<>(); // Lưu danh sách Map courseId + completedLesson

        if (userId != null) {
            // Lấy ra các courseId hiện có
            List<Long> currentCourseIds = courseEntities.getContent().stream()
                    .map(CourseEntity::getId)
                    .toList();

            // 4. Lấy danh sách các Course ID mà user đã mua trong số các ID trên (QUERY 2)
            enrolledCourseIds = enrollmentRepository.findEnrolledCourseIdsByUserIdAndCourseIds(userId.intValue(), currentCourseIds, List.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.COMPLETED));

            // 5. Nếu user có mua ít nhất 1 khóa, tiến hành lấy tiến độ (QUERY 3)
            if (!enrolledCourseIds.isEmpty()) {
                List<CompletedLessonsCountEntity> completedLessonsCountEntities =
                        completedLessonCountRepository.findByUserIdAndCourseIdIn(userId.intValue(), enrolledCourseIds);

                courseProgressMap = getCourseProgressMap(completedLessonsCountEntities);
            }
        }

        // 6. Lắp ráp dữ liệu trên RAM
        final Set<Long> finalEnrolledIds = enrolledCourseIds;
        final Map<Long, Integer> finalProgressMap = courseProgressMap;

        Page<CourseListItemResponse> courseListItemResponsePage = courseEntities.map(courseEntity -> {
            CourseListItemResponse courseListItemResponse = courseMapper.toCourseListItemResponse(courseEntity);

            boolean isEnrolled = finalEnrolledIds.contains(courseEntity.getId());
            courseListItemResponse.setEnrolled(isEnrolled);

            int progressPercentage = 0;
            if (isEnrolled) {
                int completeLessons = finalProgressMap.getOrDefault(courseEntity.getId(), 0);
                int totalLesson = courseEntity.getTotalLessons() != null ? courseEntity.getTotalLessons() : 0;
                progressPercentage = ProgressUtils.calculatePercentage(completeLessons, totalLesson);
            }

            courseListItemResponse.setProgressPercentage(progressPercentage);

            return courseListItemResponse;

        });

        return PageResponse.from(courseListItemResponsePage);
    }



    private Map<Long, Integer> getCourseProgressMap(List<CompletedLessonsCountEntity> completedLessonsCountEntities){
        return completedLessonsCountEntities.stream()
                .collect(Collectors.toMap(
                        entity -> entity.getCourse().getId(),
                        CompletedLessonsCountEntity::getCompletedLessonsCount
                ));
    }

    private Boolean isEnrollCourseById(Long courseId, Long userId) {
        return enrollmentRepository.existsByUserIdAndCourseIdAndStatusIn(
                userId, courseId, List.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.COMPLETED));
    }

    private Integer getCompleteLessons(Long courseId, Long userId) {
        CompletedLessonsCountEntity completedLessonsCountEntity = completedLessonCountRepository.getByUserIdAndCourseId(userId.intValue(), courseId)
                .orElse(null);

        return completedLessonsCountEntity != null ? completedLessonsCountEntity.getCompletedLessonsCount() : 0;
    }

    public CourseDetailResponse getCourseDetail(Long userId, Long courseId) {
        CourseEntity courseEntity = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        boolean isOwner = false;
        if (userId != null) {
            isOwner = courseEntity.getInstructor().getUser().getId().longValue() == userId.longValue();
        }

        if (courseEntity.getStatus() != com.swp391.coding_platform.entity.enums.CourseStatus.APPROVED && !isOwner && !isCurrentUserAdmin()) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }

        CourseDetailResponse response = courseMapper.toCourseDetailResponse(courseEntity);

        if (userId != null) {
            boolean isEnrolled = isEnrollCourseById(courseId, userId);
            response.setEnrolled(isEnrolled);

            if (isEnrolled) {
                int completeLessons = getCompleteLessons(courseId, userId);
                int totalLesson = courseEntity.getTotalLessons() != null ? courseEntity.getTotalLessons() : 0;
                response.setProgressPercentage(ProgressUtils.calculatePercentage(completeLessons, totalLesson));
            } else {
                response.setProgressPercentage(0);
            }
        } else {
            response.setEnrolled(false);
            response.setProgressPercentage(0);
        }

        return response;
    }

    public List<CurriculumChapterResponse> getCourseCurriculum(Long userId, Long courseId) {
        CourseEntity courseEntity = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        boolean isOwner = false;
        if (userId != null) {
            isOwner = courseEntity.getInstructor().getUser().getId().longValue() == userId.longValue();
        }

        if (courseEntity.getStatus() != com.swp391.coding_platform.entity.enums.CourseStatus.APPROVED && !isOwner && !isCurrentUserAdmin()) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }

        List<ChapterEntity> chapters = chapterRepository.findByCourseIdOrderByOrderIndexAsc(courseId);

        return chapters.stream()
                .map(courseMapper::toCurriculumChapterResponse)
                .collect(Collectors.toList());
    }

    private boolean isCurrentUserAdmin() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ADMIN".equals(a.getAuthority()) || "ROLE_ADMIN".equals(a.getAuthority()));
    }

    public CourseReviewStatsResponse getCourseReviews(Long courseId, Long userId, Pageable pageable) {
        if (!courseRepository.existsById(courseId)) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }

        CourseEntity course = courseRepository.findById(courseId).orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        Page<CourseReviewEntity> reviewPage = courseReviewRepository.findByCourseIdOrderByCreatedAtDesc(courseId, pageable);
        Page<CourseReviewDto> reviewDtoPage = reviewPage.map(courseMapper::toCourseReviewDto);

        List<Object[]> starCounts = courseReviewRepository.countStarsByCourseId(courseId);
        Map<Integer, Long> starDistribution = new HashMap<>();
        // Initialize all stars with 0
        for (int i = 1; i <= 5; i++) {
            starDistribution.put(i, 0L);
        }
        for (Object[] row : starCounts) {
            Integer star = (Integer) row[0];
            Long count = ((Number) row[1]).longValue();
            starDistribution.put(star, count);
        }

        CourseReviewStatsResponse response = CourseReviewStatsResponse.builder()
                .averageRating(course.getAverageRating())
                .totalReviews(course.getTotalReviews())
                .starDistribution(starDistribution)
                .reviews(PageResponse.from(reviewDtoPage))
                .build();

        if (userId != null) {
            courseReviewRepository.findByCourseIdAndUserId(courseId, userId.intValue())
                    .ifPresent(review -> response.setMyReview(courseMapper.toCourseReviewDto(review)));
        }

        return response;
    }

    public void upsertCourseReview(Long courseId, Long userId, CourseReviewRequest request) {
        if (!isEnrollCourseById(courseId, userId)) {
            throw new AppException(ErrorCode.NOT_ENROLLED); // Or a specific error like "NOT_ENROLLED"
        }

        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        UserEntity user = userRepository.findById(userId.intValue())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        CourseReviewEntity reviewEntity = courseReviewRepository.findByCourseIdAndUserId(courseId, userId.intValue())
                .orElseGet(() -> CourseReviewEntity.builder()
                        .course(course)
                        .user(user)
                        .build());

        reviewEntity.setContent(request.getContent());
        reviewEntity.setStar(request.getStar());
        reviewEntity.setUpdatedAt(java.time.Instant.now());

        courseReviewRepository.save(reviewEntity);

        // Recalculate average rating
        List<Object[]> starCounts = courseReviewRepository.countStarsByCourseId(courseId);
        int totalReviews = 0;
        double sumStars = 0;
        for (Object[] row : starCounts) {
            Integer star = (Integer) row[0];
            Long count = ((Number) row[1]).longValue();
            totalReviews += count;
            sumStars += star * count;
        }

        if (totalReviews > 0) {
            double rawAverage = sumStars / totalReviews;
            course.setAverageRating((double) Math.round(rawAverage * 10) / 10);
            course.setTotalReviews(totalReviews);
        } else {
            course.setAverageRating(0.0);
            course.setTotalReviews(0);
        }

        courseRepository.save(course);
    }

    public LearningDetailResponse getCourseLearningDetail(Long userId, Long courseId) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        // 1. Tải danh sách bài học đã hoàn thiện bằng EntityGraph để tránh N+1
        List<ChapterEntity> chapters = chapterRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        List<LessonEntity> orderedLessons = chapters.stream()
                .flatMap(chapter -> chapter.getLessons().stream())
                .toList();

        if (orderedLessons.isEmpty()) {
            throw new AppException(ErrorCode.LESSON_NOT_FOUND);
        }

        // 2. Tải danh sách ID bài học hoàn thành trong 1 query
        Set<Integer> completedLessonIds = lessonProgressRepository.findCompletedLessonIds(userId.intValue(), courseId);

        // 3. Tính toán tiến độ bài học hoàn thành
        int completeLessons = completedLessonIds.size();
        int totalLessons = orderedLessons.size();
        int progressPercentage = ProgressUtils.calculatePercentage(completeLessons, totalLessons);

        // 4. Tìm bài học active chưa học đầu tiên, nếu đã học hết chọn bài cuối cùng
        LessonEntity activeLesson = orderedLessons.stream()
                .filter(lesson -> !completedLessonIds.contains(lesson.getId().longValue()))
                .findFirst()
                .orElse(orderedLessons.get(orderedLessons.size() - 1));

        // 5. Trả về DTO map đa nguồn sạch sẽ bằng MapStruct
        return courseMapper.toLearningDetailResponse(course, progressPercentage, activeLesson);
    }

    public List<LearningCurriculumChapterResponse> getCourseLearningCurriculum(Long userId, Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }

        // 1. Lấy toàn bộ chapter + lessons trong 1 JOIN query bằng EntityGraph
        List<ChapterEntity> chapters = chapterRepository.findByCourseIdOrderByOrderIndexAsc(courseId);

        // 2. Lấy toàn bộ bài học đã hoàn thiện trong 1 query duy nhất
        Set<Integer> completedLessonIds = lessonProgressRepository.findCompletedLessonIds(userId.intValue(), courseId);

        // 3. Sử dụng MapStruct map kèm ngữ cảnh completedLessonIds để tự động phân phối map trạng thái hoàn thành
        return courseMapper.toLearningCurriculumChapterResponses(chapters, completedLessonIds);
    }

    public LearningLessonResponse getLearningLessonDetail(Long userId, Long courseId, Integer lessonId) {
        List<ChapterEntity> chapters = chapterRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        LessonEntity lesson = chapters.stream()
                .flatMap(ch -> ch.getLessons().stream())
                .filter(l -> l.getId().equals(lessonId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        LearningLessonResponse response = courseMapper.toLearningLessonResponse(lesson);
        response.setSourceCode(null);

        if (lesson.getStatus() == com.swp391.coding_platform.entity.enums.LessonStatus.INACTIVE ||
                lesson.getStatus() == com.swp391.coding_platform.entity.enums.LessonStatus.PENDING_UPDATE) {
            response.setProblems(new ArrayList<>());
            response.setQuiz(null);
            return response;
        }

        // Fetch coding problems linked to this lesson
        List<com.swp391.coding_platform.entity.course.LessonProblemEntity> lessonProblems = 
                lessonProblemRepository.findByLessonIdOrderByOrderIndexAsc(lessonId);
        List<com.swp391.coding_platform.dto.response.ProblemListItemResponse> problemResponses = lessonProblems.stream()
                .map(lp -> {
                    var problem = lp.getProblem();
                    String diff = problem.getCurrentVersion().getDifficulty() != null 
                            ? problem.getCurrentVersion().getDifficulty().name().substring(0, 1).toUpperCase() + problem.getCurrentVersion().getDifficulty().name().substring(1).toLowerCase()
                            : "Medium";
                    return com.swp391.coding_platform.dto.response.ProblemListItemResponse.builder()
                            .id(problem.getId())
                            .title(problem.getCurrentVersion().getTitle())
                            .difficulty(diff)
                            .score(problem.getScore() != null ? problem.getScore().intValue() : 0)
                            .totalSubmission(problem.getTotalSubmission() != null ? problem.getTotalSubmission() : 0)
                            .totalAccepted(problem.getTotalAccepted() != null ? problem.getTotalAccepted() : 0)
                            .isSolved(false)
                            .status("unsolved")
                            .build();
                })
                .toList();
        response.setProblems(problemResponses);

        // Fetch quiz details if present
        try {
            if (quizRepository.findByLessonId(lessonId).isPresent()) {
                com.swp391.coding_platform.dto.response.QuizDetailResponse quizDetail = 
                        quizService.getQuizDetailByLessonId(lessonId, userId != null ? userId.intValue() : 0);
                response.setQuiz(quizDetail);
            }
        } catch (Exception e) {
            log.warn("[CourseService] Failed to load quiz details for lessonId: {}, error: {}", lessonId, e.getMessage());
        }

        return response;
    }

    public List<LessonCommentResponse> getLessonComments(Integer lessonId) {
        List<LessonCommentEntity> roots = lessonCommentRepository.findRootCommentsWithRepliesAndUsers(lessonId);
        return roots.stream().map(this::mapCommentToResponse).toList();
    }

    @Transactional
    public LessonCommentResponse addLessonComment(Integer lessonId, Integer userId, CreateCommentRequest request) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        LessonCommentEntity parent = null;
        if (request.getParentId() != null) {
            parent = lessonCommentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

            if (!parent.getLesson().getId().equals(lessonId)) {
                throw new AppException(ErrorCode.INVALID_COMMENT_LESSON);
            }

            // Enforce maximum 1-level deep replies
            if (parent.getParent() != null) {
                throw new AppException(ErrorCode.INVALID_COMMENT_LEVEL);
            }
        }

        LessonCommentEntity comment = LessonCommentEntity.builder()
                .lesson(lesson)
                .user(user)
                .content(request.getContent())
                .parent(parent)
                .build();

        lessonCommentRepository.save(comment);
        return mapCommentToResponse(comment);
    }

    private LessonCommentResponse mapCommentToResponse(LessonCommentEntity entity) {
        List<LessonCommentResponse> replies = entity.getReplies() != null ? 
                entity.getReplies().stream().map(this::mapCommentToResponse).toList() : List.of();

        return LessonCommentResponse.builder()
                .id(entity.getId())
                .author(entity.getUser().getDisplayname())
                .avatarUrl(entity.getUser().getAvatarurl())
                .text(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .replies(replies)
                .build();
    }

    @Transactional
    public void completeLesson(Long userId, Long courseId, Integer lessonId) {
        // 1. Kiểm tra sự tồn tại của Course & Lesson
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        // 2. Kiểm tra Lesson có thuộc Course không
        if (lesson.getChapter() == null || lesson.getChapter().getCourse() == null || 
            !lesson.getChapter().getCourse().getId().equals(courseId)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // 3. Lấy Pessimistic Lock trên Enrollment của User để đồng bộ hóa, tránh race condition
        EnrollmentEntity enrollment = enrollmentRepository.findEnrollmentWithLock(userId.intValue(), courseId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_ENROLLED));

        // 4. Kiểm tra xem bài học đã hoàn thành chưa
        boolean isAlreadyCompleted = lessonProgressRepository.existsByLessonIdAndUserId(lessonId, userId.intValue());
        if (isAlreadyCompleted) {
            log.info("[completeLesson] Lesson {} already completed by user {}", lessonId, userId);
            return; // Idempotent
        }

        // 5. Thêm bản ghi tiến độ bài học (lesson_progress)
        UserEntity user = userRepository.findById(userId.intValue())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        LessonProgressEntity progress = LessonProgressEntity.builder()
                .user(user)
                .course(course)
                .lessonId(lessonId)
                .completedAt(Instant.now())
                .build();
        lessonProgressRepository.save(progress);

        // 6. Cập nhật hoặc khởi tạo tổng số bài học đã hoàn thành (completed_lessons_count)
        CompletedLessonsCountEntity countEntity = completedLessonCountRepository.getByUserIdAndCourseId(userId.intValue(), courseId)
                .orElseGet(() -> CompletedLessonsCountEntity.builder()
                        .user(user)
                        .course(course)
                        .completedLessonsCount(0)
                        .build());

        countEntity.setCompletedLessonsCount(countEntity.getCompletedLessonsCount() + 1);
        completedLessonCountRepository.save(countEntity);
        log.info("[completeLesson] User {} completed lesson {} in course {}. Completed count: {}", 
                userId, lessonId, courseId, countEntity.getCompletedLessonsCount());

        // 7. Kiểm tra bài học cuối cùng và cập nhật EnrollmentStatus thành COMPLETED
        if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
            int totalLessons = course.getTotalLessons() != null ? course.getTotalLessons() : 0;
            if (totalLessons == 0 && course.getChapters() != null) {
                for (ChapterEntity chapter : course.getChapters()) {
                    if (chapter.getLessons() != null) {
                        totalLessons += chapter.getLessons().size();
                    }
                }
            }
            if (totalLessons > 0 && countEntity.getCompletedLessonsCount() >= totalLessons) {
                enrollment.setStatus(EnrollmentStatus.COMPLETED);
                enrollmentRepository.save(enrollment);
                log.info("[completeLesson] User {} completed all {} lessons of course {}. Enrollment status updated to COMPLETED.", 
                        userId, totalLessons, courseId);
            }
        }
    }
}

