package com.swp391.coding_platform.service.contest;

import com.swp391.coding_platform.dto.request.ContestSearchRequest;
import com.swp391.coding_platform.dto.request.ContestRegisterRequest;
import com.swp391.coding_platform.dto.request.AdminContestRequest;
import com.swp391.coding_platform.dto.request.AdminContestProblemRequest;
import com.swp391.coding_platform.dto.response.ContestProblemResponse;
import com.swp391.coding_platform.dto.response.ContestResponse;
import com.swp391.coding_platform.dto.response.ContestUserStatsResponse;
import com.swp391.coding_platform.dto.response.PageResponse;
import com.swp391.coding_platform.dto.response.AdminContestResponse;
import com.swp391.coding_platform.dto.response.AdminContestProblemResponse;
import com.swp391.coding_platform.entity.contest.ContestEntity;
import com.swp391.coding_platform.entity.contest.ContestParticipantEntity;
import com.swp391.coding_platform.entity.contest.ContestProblemEntity;
import com.swp391.coding_platform.entity.contest.ContestProblemAttemptEntity;
import com.swp391.coding_platform.entity.enums.ContestStatus;
import com.swp391.coding_platform.entity.enums.ScoringRule;
import com.swp391.coding_platform.entity.enums.ProblemScope;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.mapper.ContestMapper;
import com.swp391.coding_platform.repository.contest.ContestParticipantRepository;
import com.swp391.coding_platform.repository.contest.ContestRepository;
import com.swp391.coding_platform.repository.contest.ContestProblemRepository;
import com.swp391.coding_platform.repository.contest.ContestProblemAttemptRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import com.swp391.coding_platform.repository.problem.ProblemSubmissionRepository;
import com.swp391.coding_platform.repository.problem.ProblemRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import com.swp391.coding_platform.dto.response.ContestSubmissionResponse;
import com.swp391.coding_platform.dto.response.ContestProblemDetailResponse;
import com.swp391.coding_platform.entity.problem.ProblemSubmissionEntity;
import com.swp391.coding_platform.repository.problem.ProblemTagMappingRepository;
import com.swp391.coding_platform.entity.problem.ProblemTagMappingEntity;
import com.swp391.coding_platform.entity.enums.OjVerdict;

@lombok.extern.slf4j.Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContestService {

    ContestRepository contestRepository;
    ContestMapper contestMapper;
    UserRepository userRepository;
    ProblemSubmissionRepository problemSubmissionRepository;
    ContestParticipantRepository contestParticipantRepository;
    ContestProblemRepository contestProblemRepository;
    ContestProblemAttemptRepository contestProblemAttemptRepository;
    ProblemRepository problemRepository;
    ProblemTagMappingRepository problemTagMappingRepository;
    PasswordEncoder passwordEncoder;
    com.swp391.coding_platform.repository.contest.ContestRankingRepository contestRankingRepository;
    org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;


    private String calculateStatus(ContestEntity contest, Instant now) {
        if (contest.getStatus() == ContestStatus.DELETED) {
            return "DELETED";
        }
        if (contest.getStatus() == ContestStatus.DRAFT) {
            return "DRAFT";
        }
        if (now.isBefore(contest.getStartTime())) {
            return "UPCOMING";
        } else if (now.isAfter(contest.getEndTime())) {
            return "ENDED";
        } else {
            return "ONGOING";
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<ContestResponse> getContests(ContestSearchRequest request, Integer userId) {
        String statusFilter = request.getStatus();
        String accessFilter = request.getAccess();

        // Secure sort by whitelist
        String sortByField = "id";
        if (request.getSortBy() != null) {
            String requestedSortBy = request.getSortBy().trim();
            if (List.of("id", "title", "startTime", "endTime", "durations", "createdAt", "updatedAt").contains(requestedSortBy)) {
                sortByField = requestedSortBy;
            }
        }

        Sort.Direction direction = Sort.Direction.DESC;
        if (request.getSortDirection() != null && request.getSortDirection().equalsIgnoreCase("asc")) {
            direction = Sort.Direction.ASC;
        }

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by(direction, sortByField));

        Instant now = Instant.now();
        Page<Object[]> contestPage = contestRepository.searchContestsWithStats(
                request.getSearch(),
                statusFilter != null ? statusFilter : "All",
                now,
                accessFilter != null ? accessFilter : "All",
                pageable
        );

        Page<ContestResponse> responsePage = contestPage.map(array -> {
            ContestEntity entity = (ContestEntity) array[0];
            Long partCount = (Long) array[1];
            Long probCount = (Long) array[2];

            ContestResponse response = contestMapper.toContestResponse(entity);
            response.setStatus(calculateStatus(entity, now));
            response.setParticipantCount(partCount != null ? partCount.intValue() : 0);
            response.setProblemCount(probCount != null ? probCount.intValue() : 0);

            if (userId != null) {
                response.setIsUserRegistered(contestRepository.isUserRegistered(entity.getId(), userId));
            } else {
                response.setIsUserRegistered(false);
            }
            return response;
        });

        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public ContestResponse getBannerContest(Integer userId) {
        Pageable limitOne = PageRequest.of(0, 1);
        Instant now = Instant.now();
        Page<ContestEntity> upcomingPage = contestRepository.findUpcomingContests(now, limitOne);
        
        if (upcomingPage.isEmpty()) {
            return null;
        }

        ContestEntity bannerEntity = upcomingPage.getContent().get(0);

        long partCount = contestRepository.countParticipants(bannerEntity.getId());
        long probCount = contestRepository.countProblems(bannerEntity.getId());
        boolean isReg = false;
        if (userId != null) {
            isReg = contestRepository.isUserRegistered(bannerEntity.getId(), userId);
        }

        ContestResponse response = contestMapper.toContestResponse(bannerEntity);
        response.setStatus(calculateStatus(bannerEntity, now));
        response.setParticipantCount((int) partCount);
        response.setProblemCount((int) probCount);
        response.setIsUserRegistered(isReg);

        return response;
    }

    @Transactional(readOnly = true)
    public ContestUserStatsResponse getUserStats(Integer userId) {
        if (userId == null) {
            return null;
        }
        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return null;
        }
        var user = userOpt.get();
        Integer rank = userRepository.getUserRanking(user.getId());
        long totalUsers = userRepository.count();
        long contestsCount = contestRepository.countUserContests(user.getId());

        var submissions = problemSubmissionRepository.findByUserId(user.getId());
        int totalSub = submissions.size();
        int accSub = (int) submissions.stream()
                .filter(s -> s.getVerdict() == com.swp391.coding_platform.entity.enums.OjVerdict.ACCEPTED)
                .count();
        int accuracy = totalSub > 0 ? (accSub * 100 / totalSub) : 0;

        return ContestUserStatsResponse.builder()
                .displayName(user.getDisplayname())
                .avatarUrl(user.getAvatarurl())
                .score(user.getScore())
                .rank(rank != null ? rank : 0)
                .totalUsers(totalUsers)
                .contestsCount(contestsCount)
                .avgAccuracy(accuracy)
                .build();
    }

    @Transactional(readOnly = true)
    public ContestResponse getContestById(Integer contestId, Integer userId) {
        ContestEntity entity = contestRepository.findById(contestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTEST_NOT_FOUND));

        long partCount = contestRepository.countParticipants(contestId);
        long probCount = contestRepository.countProblems(contestId);
        boolean isReg = false;
        if (userId != null) {
            isReg = contestRepository.isUserRegistered(contestId, userId);
        }

        ContestResponse response = contestMapper.toContestResponse(entity);
        response.setStatus(calculateStatus(entity, Instant.now()));
        response.setParticipantCount((int) partCount);
        response.setProblemCount((int) probCount);
        response.setIsUserRegistered(isReg);

        return response;
    }

    @Transactional
    public void registerForContest(Integer contestId, Integer userId, ContestRegisterRequest request) {
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        var contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTEST_NOT_FOUND));

        if (calculateStatus(contest, java.time.Instant.now()).equals("ENDED")) {
            throw new AppException(ErrorCode.CONTEST_ALREADY_ENDED);
        }

        boolean alreadyRegistered = contestRepository.isUserRegistered(contestId, userId);
        if (alreadyRegistered) {
            return;
        }

        // Verify password if contest is private
        if (contest.getPasswordHash() != null && !contest.getPasswordHash().trim().isEmpty()) {
            if (request == null || request.getPassword() == null ||
                    !passwordEncoder.matches(request.getPassword(), contest.getPasswordHash())) {
                throw new AppException(ErrorCode.CONTEST_PASSWORD_INVALID);
            }
        }

        ContestParticipantEntity participant = ContestParticipantEntity.builder()
                .contest(contest)
                .user(user)
                .joinedAt(java.time.Instant.now())
                .build();

        contestParticipantRepository.save(participant);
    }

    @Transactional(readOnly = true)
    public List<ContestProblemResponse> getContestProblems(Integer contestId, Integer userId, boolean isAdmin) {
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        var contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTEST_NOT_FOUND));

        if (!isAdmin) {
            boolean isRegistered = contestRepository.isUserRegistered(contestId, userId);
            if (!isRegistered) {
                throw new AppException(ErrorCode.CONTEST_NOT_JOINED);
            }
        }
        // Block access if contest has not started yet (UPCOMING)
        // ENDED is allowed: users can review problems after contest ends
        String currentStatus = calculateStatus(contest, java.time.Instant.now());
        if (currentStatus.equals("UPCOMING")) {
            throw new AppException(ErrorCode.CONTEST_NOT_STARTED);
        }

        List<ContestProblemEntity> contestProblems = contestProblemRepository.findByContestIdWithProblem(contestId);
        List<ProblemSubmissionEntity> submissions = problemSubmissionRepository.findByContestIdAndUserId(contestId, userId);

        return contestProblems.stream().map(cp -> {
            var problem = cp.getProblem();

            // Find user's submissions for this specific problem
            List<ProblemSubmissionEntity> problemSubs = submissions.stream()
                    .filter(s -> s.getProblem().getId().equals(problem.getId()))
                    .toList();

            String status = "UNATTEMPTED";
            if (!problemSubs.isEmpty()) {
                boolean isSolved = problemSubs.stream().anyMatch(s -> s.getVerdict() == com.swp391.coding_platform.entity.enums.OjVerdict.ACCEPTED);
                if (isSolved) {
                    status = "SOLVED";
                } else {
                    status = "FAILED";
                }
            }

            return ContestProblemResponse.builder()
                    .problemId(problem.getId())
                    .title(problem.getCurrentVersion().getTitle())
                    .orderIndex(cp.getOrderIndex())
                    .difficulty(problem.getCurrentVersion().getDifficulty() != null ? problem.getCurrentVersion().getDifficulty().name() : "MEDIUM")
                    .totalSubmission(problem.getTotalSubmission())
                    .totalAccepted(problem.getTotalAccepted())
                    .status(status)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminContestResponse> getAdminContests() {
        List<Object[]> results = contestRepository.getAdminContestsWithStats();
        Instant now = Instant.now();
        return results.stream().map(array -> {
            ContestEntity entity = (ContestEntity) array[0];
            Long partCount = (Long) array[1];
            Long probCount = (Long) array[2];
            Long subCount = (Long) array[3];
            Double avgScore = (Double) array[4];

            AdminContestResponse response = contestMapper.toAdminContestResponse(entity);
            response.setStatus(calculateStatus(entity, now));
            response.setParticipantCount(partCount != null ? partCount.intValue() : 0);
            response.setProblemCount(probCount != null ? probCount.intValue() : 0);
            response.setSubmissionCount(subCount != null ? subCount.intValue() : 0);
            response.setAverageScore(avgScore != null ? avgScore : 0.0);
            return response;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminContestResponse getAdminContestById(Integer id) {
        ContestEntity entity = contestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONTEST_NOT_FOUND));

        long partCount = contestRepository.countParticipants(id);
        long probCount = contestRepository.countProblems(id);
        long subCount = problemSubmissionRepository.countByContestId(id);
        Double avgScore = problemSubmissionRepository.getAverageScoreByContestId(id);

        AdminContestResponse response = contestMapper.toAdminContestResponse(entity);
        response.setStatus(calculateStatus(entity, Instant.now()));
        response.setParticipantCount((int) partCount);
        response.setProblemCount((int) probCount);
        response.setSubmissionCount((int) subCount);
        response.setAverageScore(avgScore != null ? avgScore : 0.0);
        return response;
    }

    @Transactional
    public AdminContestResponse createAdminContest(AdminContestRequest request, Integer adminUserId) {
        var creator = userRepository.findById(adminUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String passwordHash = null;
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            passwordHash = passwordEncoder.encode(request.getPassword().trim());
        }

        long durationMinutes = java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();

        ContestEntity contest = ContestEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .scoringRule(ScoringRule.valueOf(request.getScoringRule()))
                .passwordHash(passwordHash)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .durations((int) durationMinutes)
                .status(ContestStatus.DRAFT)
                .createdBy(creator)
                .build();

        ContestEntity saved = contestRepository.save(contest);
        return getAdminContestById(saved.getId());
    }

    @Transactional
    public AdminContestResponse updateAdminContest(Integer id, AdminContestRequest request) {
        ContestEntity contest = contestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONTEST_NOT_FOUND));

        Instant now = Instant.now();
        String currentStatus = calculateStatus(contest, now);

        if (currentStatus.equals("DELETED")) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (currentStatus.equals("ONGOING") || currentStatus.equals("ENDED")) {
            boolean timesChanged = !contest.getStartTime().equals(request.getStartTime()) ||
                                   !contest.getEndTime().equals(request.getEndTime());
            boolean scoringRuleChanged = contest.getScoringRule() != ScoringRule.valueOf(request.getScoringRule());
            if (timesChanged || scoringRuleChanged) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            contest.setTitle(request.getTitle());
            contest.setDescription(request.getDescription());
        } else {
            contest.setTitle(request.getTitle());
            contest.setDescription(request.getDescription());
            contest.setScoringRule(ScoringRule.valueOf(request.getScoringRule()));
            contest.setStartTime(request.getStartTime());
            contest.setEndTime(request.getEndTime());

            long durationMinutes = java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
            contest.setDurations((int) durationMinutes);

            if (request.getPassword() != null) {
                if (request.getPassword().trim().isEmpty()) {
                    contest.setPasswordHash(null);
                } else {
                    contest.setPasswordHash(passwordEncoder.encode(request.getPassword().trim()));
                }
            }
        }

        contestRepository.save(contest);
        return getAdminContestById(id);
    }

    @Transactional
    public void deleteAdminContest(Integer id) {
        ContestEntity contest = contestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONTEST_NOT_FOUND));

        Instant now = Instant.now();
        String currentStatus = calculateStatus(contest, now);
        if (!currentStatus.equals("DRAFT") && !currentStatus.equals("UPCOMING")) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        contest.setStatus(ContestStatus.DELETED);
        contestRepository.save(contest);
    }

    @Transactional
    public AdminContestResponse publishAdminContest(Integer id) {
        ContestEntity contest = contestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONTEST_NOT_FOUND));

        if (contest.getStatus() != ContestStatus.DRAFT) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        contest.setStatus(ContestStatus.PUBLISHED);
        contestRepository.save(contest);
        return getAdminContestById(id);
    }

    @Transactional
    public AdminContestResponse restoreAdminContest(Integer id) {
        ContestEntity contest = contestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONTEST_NOT_FOUND));

        if (contest.getStatus() != ContestStatus.DELETED) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        contest.setStatus(ContestStatus.DRAFT);
        contestRepository.save(contest);
        return getAdminContestById(id);
    }

    @Transactional
    public void hardDeleteAdminContest(Integer id) {
        ContestEntity contest = contestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONTEST_NOT_FOUND));

        if (contest.getStatus() != ContestStatus.DELETED) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        long subCount = problemSubmissionRepository.countByContestId(id);
        if (subCount > 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        contestParticipantRepository.deleteByContestId(id);
        contestProblemRepository.deleteByContestId(id);
        contestRepository.delete(contest);
    }

    @Transactional(readOnly = true)
    public List<AdminContestProblemResponse> getAdminContestProblems(Integer contestId) {
        List<ContestProblemEntity> contestProblems = contestProblemRepository.findByContestIdWithProblem(contestId);
        return contestProblems.stream()
                .map(contestMapper::toAdminContestProblemResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addProblemToContest(Integer contestId, AdminContestProblemRequest request) {
        var contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTEST_NOT_FOUND));

        Instant now = Instant.now();
        String currentStatus = calculateStatus(contest, now);
        if (currentStatus.equals("ONGOING") || currentStatus.equals("ENDED") || currentStatus.equals("DELETED")) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        var problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND));

        boolean exists = contestProblemRepository.existsByContestIdAndProblemId(contestId, request.getProblemId());
        if (exists) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        ContestProblemEntity cp = ContestProblemEntity.builder()
                .contest(contest)
                .problem(problem)
                .problemVersion(problem.getCurrentVersion())
                .orderIndex(request.getOrderIndex())
                .build();

        contestProblemRepository.save(cp);
    }

    @Transactional
    public void removeProblemFromContest(Integer contestId, Integer problemId) {
        var contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTEST_NOT_FOUND));

        Instant now = Instant.now();
        String currentStatus = calculateStatus(contest, now);
        if (currentStatus.equals("ONGOING") || currentStatus.equals("ENDED") || currentStatus.equals("DELETED")) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        ContestProblemEntity cp = contestProblemRepository.findByContestIdAndProblemId(contestId, problemId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        contestProblemRepository.delete(cp);
    }

    @Transactional(readOnly = true)
    public List<ContestSubmissionResponse> getContestSubmissions(Integer contestId, Integer userId, boolean isAdmin) {
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        ContestEntity contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTEST_NOT_FOUND));

        List<ProblemSubmissionEntity> submissions;
        if (isAdmin) {
            submissions = problemSubmissionRepository.findByContestId(contestId);
        } else {
            boolean isRegistered = contestRepository.isUserRegistered(contestId, userId);
            if (!isRegistered) {
                throw new AppException(ErrorCode.CONTEST_NOT_JOINED);
            }
            // Block access if contest has not started yet
            String currentStatus = calculateStatus(contest, java.time.Instant.now());
            if (currentStatus.equals("UPCOMING")) {
                throw new AppException(ErrorCode.CONTEST_NOT_STARTED);
            }
            submissions = problemSubmissionRepository.findByContestIdAndUserId(contestId, userId);
        }

        List<ContestProblemEntity> cpList = contestProblemRepository.findByContestIdWithProblem(contestId);
        Map<Integer, String> problemLabelMap = new HashMap<>();
        for (ContestProblemEntity cp : cpList) {
            char label = (char) ('A' + cp.getOrderIndex());
            problemLabelMap.put(cp.getProblem().getId(), String.valueOf(label));
        }

        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(java.time.ZoneId.systemDefault());

        return submissions.stream().map(s -> {
            String label = problemLabelMap.getOrDefault(s.getProblem().getId(), "?");
            
            String subStatus = s.getVerdict() == com.swp391.coding_platform.entity.enums.OjVerdict.ACCEPTED ? "Accepted"
                    : s.getVerdict().name().replace("_", " ");
            subStatus = Arrays.stream(subStatus.split(" "))
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                    .collect(Collectors.joining(" "));

            String langStr;
            switch (s.getLanguageId()) {
                case 50: langStr = "C"; break;
                case 54: langStr = "C++"; break;
                case 62: langStr = "Java"; break;
                case 71: langStr = "Python 3"; break;
                case 51: langStr = "C#"; break;
                case 63: langStr = "JavaScript"; break;
                default: langStr = "Java"; break;
            }

            String runtimeStr = s.getExecutionTime() != null ? String.format(Locale.US, "%.1f ms", (double) s.getExecutionTime())
                    : "N/A";
            String memoryStr = s.getMemoryUsed() != null
                    ? String.format(Locale.US, "%.1f MB", s.getMemoryUsed() / 1024.0)
                    : "N/A";
            String timeStr = formatter.format(s.getSubmittedAt());
            String statusClass = s.getVerdict() == com.swp391.coding_platform.entity.enums.OjVerdict.ACCEPTED ? "text-brand-green" : "text-red-600";

            return ContestSubmissionResponse.builder()
                    .id(s.getId())
                    .submittedAt(timeStr)
                    .username(s.getUser().getUsername())
                    .displayName(s.getUser().getDisplayname() != null ? s.getUser().getDisplayname() : s.getUser().getUsername())
                    .problemLabel(label)
                    .problemId(s.getProblem().getId())
                    .problemTitle(s.getProblem().getCurrentVersion().getTitle())
                    .status(subStatus)
                    .lang(langStr)
                    .runtime(runtimeStr)
                    .memory(memoryStr)
                    .statusClass(statusClass)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContestProblemDetailResponse getContestProblemDetail(Integer contestId, Integer problemId, Integer userId, boolean isAdmin) {
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        ContestEntity contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTEST_NOT_FOUND));

        // Verify user registration
        if (!isAdmin) {
            boolean isRegistered = contestRepository.isUserRegistered(contestId, userId);
            if (!isRegistered) {
                throw new AppException(ErrorCode.CONTEST_NOT_JOINED);
            }
        }

        // Verify contest has started (throw 403 / CONTEST_NOT_STARTED if upcoming)
        Instant now = Instant.now();
        String currentStatus = calculateStatus(contest, now);
        if (currentStatus.equals("UPCOMING")) {
            throw new AppException(ErrorCode.CONTEST_NOT_STARTED);
        }

        // Check if the problem belongs to the contest
        ContestProblemEntity cp = contestProblemRepository.findByContestIdAndProblemId(contestId, problemId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        ProblemEntity problem = cp.getProblem();

        List<ProblemTagMappingEntity> mappings = problemTagMappingRepository.findByProblemId(problemId);
        List<String> tags = mappings.stream().map(m -> m.getTag().getName()).toList();

        Map<String, String> templates = new HashMap<>();
        if (problem.getCurrentVersion().getStarterTemplates() != null && !problem.getCurrentVersion().getStarterTemplates().isBlank()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                templates = objectMapper.readValue(problem.getCurrentVersion().getStarterTemplates(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
            } catch (Exception e) {
                // Ignore parse errors
            }
        }
        String attemptStatus = "unsolved";
        String sourceCode = null;
        Integer languageId = null;

        // Fetch user's submissions in this contest for this problem
        List<ProblemSubmissionEntity> subs = problemSubmissionRepository.findByContestIdAndUserId(contestId, userId);
        if (subs != null && !subs.isEmpty()) {
            List<ProblemSubmissionEntity> problemSubs = subs.stream()
                    .filter(s -> s.getProblem().getId().equals(problemId))
                    .sorted(Comparator.comparing(ProblemSubmissionEntity::getSubmittedAt).reversed())
                    .toList();
            if (!problemSubs.isEmpty()) {
                boolean isSolved = problemSubs.stream().anyMatch(s -> s.getVerdict() == com.swp391.coding_platform.entity.enums.OjVerdict.ACCEPTED);
                if (isSolved) {
                    attemptStatus = "solved";
                } else {
                    attemptStatus = "attempted";
                }

                Optional<ProblemSubmissionEntity> acceptedOpt = problemSubs.stream().filter(s -> s.getVerdict() == com.swp391.coding_platform.entity.enums.OjVerdict.ACCEPTED).findFirst();
                if (acceptedOpt.isPresent()) {
                    sourceCode = acceptedOpt.get().getSourceCode();
                    languageId = acceptedOpt.get().getLanguageId();
                } else {
                    sourceCode = problemSubs.get(0).getSourceCode();
                    languageId = problemSubs.get(0).getLanguageId();
                }
            }
        }

        String difficultyStr = "Medium";
        if (problem.getCurrentVersion().getDifficulty() != null) {
            String name = problem.getCurrentVersion().getDifficulty().name();
            difficultyStr = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        }

        String acceptance = "0.0%";
        if (problem.getTotalSubmission() != null && problem.getTotalSubmission() > 0) {
            double rate = (problem.getTotalAccepted() * 100.0) / problem.getTotalSubmission();
            acceptance = String.format(Locale.US, "%.1f%%", rate);
        }
        Integer totalSolved = problem.getTotalAccepted() != null ? problem.getTotalAccepted() : 0;

        char labelChar = (char) ('A' + cp.getOrderIndex());

        return ContestProblemDetailResponse.builder()
                .id(problem.getId())
                .title(problem.getCurrentVersion().getTitle())
                .difficulty(difficultyStr)
                .description(problem.getCurrentVersion().getDescription())
                .inputDescription(problem.getCurrentVersion().getInputDescription())
                .outputDescription(problem.getCurrentVersion().getOutputDescription())
                .constraints(problem.getCurrentVersion().getConstraints())
                .exampleInput(problem.getCurrentVersion().getExampleInput())
                .exampleOutput(problem.getCurrentVersion().getExampleOutput())
                .tags(tags)
                .templates(templates)
                .status(attemptStatus)
                .acceptance(acceptance)
                .totalSolved(totalSolved)
                .sourceCode(sourceCode)
                .languageId(languageId)
                .problemLabel(String.valueOf(labelChar))
                .timeLimitMs(problem.getCurrentVersion().getTimeLimitMs())
                .memoryLimitKb(problem.getCurrentVersion().getMemoryLimitKb())
                .build();
    }

    @Transactional(readOnly = true)
    public com.swp391.coding_platform.dto.response.MyContestStatsResponse getMyContestStats(Integer userId) {
        if (userId == null) {
            return null;
        }

        List<com.swp391.coding_platform.entity.contest.ContestParticipantEntity> registrations =
                contestParticipantRepository.findByUserIdWithContest(userId);

        long totalContests = registrations.size();
        int top1 = 0;
        int top2 = 0;
        int top3 = 0;

        if (totalContests > 0) {
            List<Integer> contestIds = registrations.stream()
                    .map(r -> r.getContest().getId())
                    .collect(Collectors.toList());

            List<Object[]> partCounts = contestParticipantRepository.countParticipantsByContestIds(contestIds);
            Map<Integer, Long> partCountMap = partCounts.stream()
                    .collect(Collectors.toMap(
                            row -> (Integer) row[0],
                            row -> (Long) row[1]
                    ));

            List<com.swp391.coding_platform.entity.contest.ContestRankingEntity> userRankings =
                    contestRankingRepository.findByUserIdAndContestIds(userId, contestIds);
            Map<Integer, com.swp391.coding_platform.entity.contest.ContestRankingEntity> rankingMap = userRankings.stream()
                    .collect(Collectors.toMap(
                            r -> r.getContest().getId(),
                            r -> r
                    ));

            for (Integer contestId : contestIds) {
                int rank = calculateUserRank(contestId, userId, rankingMap.get(contestId), partCountMap.getOrDefault(contestId, 0L));
                if (rank == 1) {
                    top1++;
                } else if (rank == 2) {
                    top2++;
                } else if (rank == 3) {
                    top3++;
                }
            }
        }

        return com.swp391.coding_platform.dto.response.MyContestStatsResponse.builder()
                .totalContests(totalContests)
                .top1Count(top1)
                .top2Count(top2)
                .top3Count(top3)
                .build();
    }

    @Transactional(readOnly = true)
    public List<com.swp391.coding_platform.dto.response.MyContestHistoryResponse> getMyContestHistory(Integer userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        List<com.swp391.coding_platform.entity.contest.ContestParticipantEntity> registrations =
                contestParticipantRepository.findByUserIdWithContest(userId);

        if (registrations.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> contestIds = registrations.stream()
                .map(r -> r.getContest().getId())
                .collect(Collectors.toList());

        List<Object[]> partCounts = contestParticipantRepository.countParticipantsByContestIds(contestIds);
        Map<Integer, Long> partCountMap = partCounts.stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> (Long) row[1]
                ));

        List<com.swp391.coding_platform.entity.contest.ContestRankingEntity> userRankings =
                    contestRankingRepository.findByUserIdAndContestIds(userId, contestIds);
        Map<Integer, com.swp391.coding_platform.entity.contest.ContestRankingEntity> rankingMap = userRankings.stream()
                .collect(Collectors.toMap(
                        r -> r.getContest().getId(),
                        r -> r
                ));

        Instant now = Instant.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(java.time.ZoneId.systemDefault());

        List<com.swp391.coding_platform.dto.response.MyContestHistoryResponse> historyList = new ArrayList<>();

        for (com.swp391.coding_platform.entity.contest.ContestParticipantEntity reg : registrations) {
            com.swp391.coding_platform.entity.contest.ContestEntity contest = reg.getContest();
            Integer contestId = contest.getId();

            com.swp391.coding_platform.entity.contest.ContestRankingEntity rankingEntity = rankingMap.get(contestId);
            long totalParticipants = partCountMap.getOrDefault(contestId, 0L);

            int rank = calculateUserRank(contestId, userId, rankingEntity, totalParticipants);
            int solved = rankingEntity != null ? rankingEntity.getProblemsSolved() : 0;
            int penalty = rankingEntity != null ? rankingEntity.getTotalPenalty() : 0;

            String status = calculateStatus(contest, now);

            historyList.add(com.swp391.coding_platform.dto.response.MyContestHistoryResponse.builder()
                    .id(contestId)
                    .title(contest.getTitle())
                    .startDate(formatter.format(contest.getStartTime()))
                    .endDate(formatter.format(contest.getEndTime()))
                    .status(status)
                    .rank(rank)
                    .totalParticipants(totalParticipants)
                    .problemsSolved(solved)
                    .score(penalty)
                    .build());
        }

        return historyList;
    }

    private int calculateUserRank(Integer contestId, Integer userId, com.swp391.coding_platform.entity.contest.ContestRankingEntity rankingEntity, long totalParticipants) {
        try {
            String zsetKey = "contest:scoreboard:" + contestId + ":live";
            Double score = stringRedisTemplate.opsForZSet().score(zsetKey, String.valueOf(userId));
            if (score != null) {
                Long revRank = stringRedisTemplate.opsForZSet().reverseRank(zsetKey, String.valueOf(userId));
                if (revRank != null) {
                    return revRank.intValue() + 1;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch live rank from Redis for user {} in contest {}: {}", userId, contestId, e.getMessage());
        }

        if (rankingEntity == null) {
            return (int) totalParticipants;
        }

        long betterCount = contestRankingRepository.countBetterRankings(
                contestId,
                rankingEntity.getProblemsSolved(),
                rankingEntity.getTotalPenalty()
        );
        return (int) (betterCount + 1);
    }
}
