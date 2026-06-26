package com.swp391.coding_platform.service.problem;

import com.swp391.coding_platform.dto.request.AdminProblemRequest;
import com.swp391.coding_platform.dto.request.CreateCommentRequest;
import com.swp391.coding_platform.dto.response.AdminProblemResponse;
import com.swp391.coding_platform.dto.response.ProblemCommentResponse;
import com.swp391.coding_platform.dto.response.ProblemDescriptionResponse;
import com.swp391.coding_platform.dto.response.ProblemListItemResponse;
import com.swp391.coding_platform.dto.response.ProblemSolutionResponse;
import com.swp391.coding_platform.entity.enums.OjVerdict;
import com.swp391.coding_platform.entity.enums.ProblemDifficulty;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.entity.enums.ProblemScope;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.problem.ProblemCommentEntity;
import com.swp391.coding_platform.entity.problem.ProblemSubmissionEntity;
import com.swp391.coding_platform.entity.problem.ProblemTagMappingEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.repository.problem.ProblemCommentRepository;
import com.swp391.coding_platform.repository.problem.ProblemRepository;
import com.swp391.coding_platform.repository.problem.ProblemSubmissionRepository;
import com.swp391.coding_platform.repository.problem.ProblemTagMappingRepository;
import com.swp391.coding_platform.repository.problem.ProblemTagRepository;
import com.swp391.coding_platform.entity.problem.ProblemTagEntity;
import com.swp391.coding_platform.dto.response.ProblemTagResponse;
import com.swp391.coding_platform.repository.user.UserRepository;
import com.swp391.coding_platform.repository.problem.ProblemTestcaseRepository;
import com.swp391.coding_platform.entity.problem.ProblemTestcaseEntity;
import com.swp391.coding_platform.dto.request.AdminTestcaseRequest;
import com.swp391.coding_platform.dto.response.AdminTestcaseResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProblemService {

    ProblemRepository problemRepository;
    ProblemTagMappingRepository problemTagMappingRepository;
    ProblemSubmissionRepository problemSubmissionRepository;
    ProblemCommentRepository problemCommentRepository;
    UserRepository userRepository;
    ProblemTestcaseRepository problemTestcaseRepository;
    ProblemTagRepository problemTagRepository;

    @lombok.experimental.NonFinal
    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    public List<ProblemListItemResponse> getProblems(Integer userId) {
        List<ProblemEntity> problems = problemRepository.findByProblemScopeInAndIsActiveTrueAndIsPublicTrue(
                List.of(ProblemScope.PRACTICE, ProblemScope.SHARED)
        );

        if (problems.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> problemIds = problems.stream().map(ProblemEntity::getId).toList();

        // Load mappings to avoid N+1
        List<ProblemTagMappingEntity> mappings = problemTagMappingRepository.findByProblemIdIn(problemIds);
        Map<Integer, List<String>> tagsByProblemId = mappings.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getProblem().getId(),
                        Collectors.mapping(m -> m.getTag().getName(), Collectors.toList())
                ));

        // Load submissions if user is logged in
        Map<Integer, List<ProblemSubmissionEntity>> submissionsByProblemId = new HashMap<>();
        if (userId != null) {
            List<ProblemSubmissionEntity> userSubmissions = problemSubmissionRepository.findByUserIdAndProblemIdIn(
                    userId.intValue(), problemIds
            );
            submissionsByProblemId = userSubmissions.stream()
                    .collect(Collectors.groupingBy(s -> s.getProblem().getId()));
        }

        final Map<Integer, List<ProblemSubmissionEntity>> finalSubmissions = submissionsByProblemId;

        return problems.stream().map(problem -> {
            List<String> tags = tagsByProblemId.getOrDefault(problem.getId(), Collections.emptyList());

            boolean isSolved = false;
            String status = "unsolved";
            List<ProblemSubmissionEntity> subs = finalSubmissions.getOrDefault(problem.getId(), Collections.emptyList());
            if (!subs.isEmpty()) {
                isSolved = subs.stream().anyMatch(s -> s.getVerdict() == OjVerdict.ACCEPTED);
                status = isSolved ? "solved" : "attempted";
            }

            String difficultyStr = "Medium";
            if (problem.getDifficulty() != null) {
                String name = problem.getDifficulty().name();
                difficultyStr = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            }

            return ProblemListItemResponse.builder()
                    .id(problem.getId())
                    .title(problem.getTitle())
                    .difficulty(difficultyStr)
                    .tags(tags)
                    .score(problem.getScore() != null ? problem.getScore().intValue() : 0)
                    .totalSubmission(problem.getTotalSubmission() != null ? problem.getTotalSubmission() : 0)
                    .totalAccepted(problem.getTotalAccepted() != null ? problem.getTotalAccepted() : 0)
                    .isSolved(isSolved)
                    .status(status)
                    .build();
        }).toList();
    }

    public ProblemDescriptionResponse getProblemDescription(Integer id, Integer userId) {
        ProblemEntity problem = problemRepository.findByIdAndIsPublicTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND));

        List<ProblemTagMappingEntity> mappings = problemTagMappingRepository.findByProblemId(id);
        List<String> tags = mappings.stream().map(m -> m.getTag().getName()).toList();

        Map<String, String> templates = new HashMap<>();
        if (problem.getStarterTemplates() != null && !problem.getStarterTemplates().isBlank()) {
            try {
                templates = objectMapper.readValue(problem.getStarterTemplates(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
            } catch (Exception e) {
                log.warn("Failed to parse starter templates for problem {}: {}", id, e.getMessage());
            }
        }
        if (templates.isEmpty()) {
            templates = generateTemplates(problem.getTitle());
        }

        String status = "unsolved";
        String sourceCode = null;
        Integer languageId = null;
        if (userId != null) {
            List<ProblemSubmissionEntity> subs = problemSubmissionRepository.findByUserIdAndProblemId(userId.intValue(), id);
            if (!subs.isEmpty()) {
                subs.sort(Comparator.comparing(ProblemSubmissionEntity::getSubmittedAt).reversed());
                Optional<ProblemSubmissionEntity> acceptedOpt = subs.stream().filter(s -> s.getVerdict() == OjVerdict.ACCEPTED).findFirst();
                if (acceptedOpt.isPresent()) {
                    status = "solved";
                    sourceCode = acceptedOpt.get().getSourceCode();
                    languageId = acceptedOpt.get().getLanguageId();
                } else {
                    status = "attempted";
                    sourceCode = subs.get(0).getSourceCode();
                    languageId = subs.get(0).getLanguageId();
                }
            }
        }

        String difficultyStr = "Medium";
        if (problem.getDifficulty() != null) {
            String name = problem.getDifficulty().name();
            difficultyStr = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        }

        String acceptance = "0.0%";
        if (problem.getTotalSubmission() != null && problem.getTotalSubmission() > 0) {
            double rate = (problem.getTotalAccepted() * 100.0) / problem.getTotalSubmission();
            acceptance = String.format(Locale.US, "%.1f%%", rate);
        }
        Integer totalSolved = problem.getTotalAccepted() != null ? problem.getTotalAccepted() : 0;

        return ProblemDescriptionResponse.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .difficulty(difficultyStr)
                .description(problem.getDescription())
                .inputDescription(problem.getInputDescription())
                .outputDescription(problem.getOutputDescription())
                .constraints(problem.getConstraints())
                .exampleInput(problem.getExampleInput())
                .exampleOutput(problem.getExampleOutput())
                .hint(problem.getHint())
                .tags(tags)
                .templates(templates)
                .status(status)
                .acceptance(acceptance)
                .totalSolved(totalSolved)
                .sourceCode(sourceCode)
                .languageId(languageId)
                .build();
    }

    private Map<String, String> generateTemplates(String title) {
        Map<String, String> templates = new HashMap<>();
        String cleanTitle = title != null ? title.trim().toLowerCase() : "";

        if (cleanTitle.contains("two sum")) {
            templates.put("Java", "class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // Write your code here\n        return new int[0];\n    }\n}");
            templates.put("Python 3", "class Solution:\n    def twoSum(self, nums: List[int], target: int) -> List[int]:\n        # Write your code here\n        return []");
            templates.put("C++", "class Solution {\npublic:\n    vector<int> twoSum(vector<int>& nums, int target) {\n        // Write your code here\n        return {};\n    }\n};");
            templates.put("JavaScript", "var twoSum = function(nums, target) {\n    // Write your code here\n    return [];\n};");
        } else if (cleanTitle.contains("add two numbers")) {
            templates.put("Java", "class Solution {\n    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {\n        // Write your code here\n        return null;\n    }\n}");
            templates.put("Python 3", "class Solution:\n    def addTwoNumbers(self, l1: Optional[ListNode], l2: Optional[ListNode]) -> Optional[ListNode]:\n        # Write your code here\n        return None");
            templates.put("C++", "class Solution {\npublic:\n    ListNode* addTwoNumbers(ListNode* l1, ListNode* l2) {\n        // Write your code here\n        return nullptr;\n    }\n};");
            templates.put("JavaScript", "var addTwoNumbers = function(l1, l2) {\n    // Write your code here\n    return null;\n};");
        } else if (cleanTitle.contains("longest substring")) {
            templates.put("Java", "class Solution {\n    public int lengthOfLongestSubstring(String s) {\n        // Write your code here\n        return 0;\n    }\n}");
            templates.put("Python 3", "class Solution:\n    def lengthOfLongestSubstring(self, s: str) -> int:\n        # Write your code here\n        return 0");
            templates.put("C++", "class Solution {\npublic:\n    int lengthOfLongestSubstring(string s) {\n        // Write your code here\n        return 0;\n    }\n};");
            templates.put("JavaScript", "var lengthOfLongestSubstring = function(s) {\n    // Write your code here\n    return 0;\n};");
        } else {
            templates.put("Java", "class Solution {\n    public void solve() {\n        // Write your code here\n    }\n}");
            templates.put("Python 3", "class Solution:\n    def solve(self):\n        # Write your code here\n        pass");
            templates.put("C++", "class Solution {\npublic:\n    void solve() {\n        // Write your code here\n    }\n};");
            templates.put("JavaScript", "var solve = function() {\n    // Write your code here\n};");
        }
        return templates;
    }

    public List<ProblemCommentResponse> getComments(Integer problemId) {
        problemRepository.findByIdAndIsPublicTrue(problemId)
                .orElseThrow(() -> new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND));
        List<ProblemCommentEntity> topComments =
                problemCommentRepository.findByProblemIdAndParentIsNullOrderByCreatedAtDesc(problemId);
        return topComments.stream().map(this::mapCommentToResponse).toList();
    }

    @Transactional
    public ProblemCommentResponse addComment(Integer problemId, Integer userId, CreateCommentRequest request) {
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        ProblemEntity problem = problemRepository.findByIdAndIsPublicTrue(problemId)
                .orElseThrow(() -> new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND));

        UserEntity user = userRepository.findById(userId.intValue())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ProblemCommentEntity parent = null;
        if (request.getParentId() != null) {
            parent = problemCommentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
        }

        ProblemCommentEntity comment = ProblemCommentEntity.builder()
                .problem(problem)
                .user(user)
                .content(request.getContent())
                .parent(parent)
                .createdAt(Instant.now())
                .build();

        problemCommentRepository.save(comment);

        return mapCommentToResponse(comment);
    }

    private ProblemCommentResponse mapCommentToResponse(ProblemCommentEntity entity) {
        String author = entity.getUser().getDisplayname() != null ? entity.getUser().getDisplayname() : entity.getUser().getUsername();

        List<ProblemCommentResponse> replies = entity.getReplies() != null ?
                entity.getReplies().stream().map(this::mapCommentToResponse).toList() : Collections.emptyList();

        return ProblemCommentResponse.builder()
                .id(entity.getId())
                .author(author)
                .avatarUrl(entity.getUser().getAvatarurl())
                .text(entity.getContent())
                .time(formatTimeAgo(entity.getCreatedAt()))
                .createdAt(entity.getCreatedAt())
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .replies(replies)
                .build();
    }

    private String formatTimeAgo(Instant instant) {
        if (instant == null) return "";
        long seconds = java.time.Duration.between(instant, Instant.now()).getSeconds();
        if (seconds < 60) return "Just now";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + " " + (minutes == 1 ? "minute" : "minutes") + " ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + " " + (hours == 1 ? "hour" : "hours") + " ago";
        long days = hours / 24;
        return days + " " + (days == 1 ? "day" : "days") + " ago";
    }

    public ProblemSolutionResponse getProblemSolution(Integer id, Integer userId) {
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        ProblemEntity problem = problemRepository.findByIdAndIsPublicTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND));

        // Check if the user has solved this problem
        List<ProblemSubmissionEntity> submissions = problemSubmissionRepository.findByUserIdAndProblemId(userId.intValue(), id);
        boolean solved = submissions.stream().anyMatch(s -> s.getVerdict() == OjVerdict.ACCEPTED);

        if (!solved) {
            throw new AppException(ErrorCode.OJ_SOLUTION_LOCKED);
        }

        String solutionCode = problem.getSolutions();
        if (solutionCode == null || solutionCode.isBlank()) {
            solutionCode = "// An official solution for this problem is not available yet.";
        }

        return ProblemSolutionResponse.builder()
                .problemId(problem.getId())
                .title(problem.getTitle())
                .solutionCode(solutionCode)
                .build();
    }

    public List<AdminProblemResponse> getAdminProblems() {
        return problemRepository.findByProblemScopeIn(
                List.of(ProblemScope.CONTEST, ProblemScope.PRACTICE, ProblemScope.SHARED)
        ).stream()
                .map(this::mapToAdminResponse)
                .toList();
    }

    public List<ProblemTagResponse> getAllTags() {
        if (problemTagRepository.count() == 0) {
            List<String> defaultTags = List.of("Arrays", "Hash Map", "Dynamic Programming", "Two Pointers", "Math", "String", "Binary Search", "Greedy", "Sorting", "Trees");
            for (String tagName : defaultTags) {
                String slug = tagName.toLowerCase().replace(" ", "-");
                problemTagRepository.save(ProblemTagEntity.builder()
                        .name(tagName)
                        .slug(slug)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build());
            }
        }
        return problemTagRepository.findAll().stream()
                .map(t -> ProblemTagResponse.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .slug(t.getSlug())
                        .build())
                .toList();
    }

    @Transactional
    public AdminProblemResponse createAdminProblem(AdminProblemRequest request, Integer adminUserId) {
        UserEntity createdBy = userRepository.findById(adminUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ProblemScope scope = ProblemScope.PRACTICE;
        if (request.getProblemScope() != null) {
            try {
                scope = ProblemScope.valueOf(request.getProblemScope());
            } catch (IllegalArgumentException e) {
                // Keep default
            }
        }

        ProblemDifficulty difficulty = ProblemDifficulty.MEDIUM;
        if (request.getDifficulty() != null) {
            try {
                difficulty = ProblemDifficulty.valueOf(request.getDifficulty());
            } catch (IllegalArgumentException e) {
                // Keep default
            }
        }

        String templatesJson = null;
        if (request.getStarterTemplates() != null) {
            try {
                templatesJson = objectMapper.writeValueAsString(request.getStarterTemplates());
            } catch (Exception e) {
                log.warn("Failed to serialize starter templates: {}", e.getMessage());
            }
        }

        ProblemEntity problem = ProblemEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .inputDescription(request.getInputDescription())
                .outputDescription(request.getOutputDescription())
                .constraints(request.getConstraints())
                .exampleInput(request.getExampleInput())
                .exampleOutput(request.getExampleOutput())
                .hint(request.getHint())
                .problemScope(scope)
                .difficulty(difficulty)
                .isActive(false)
                .createdBy(createdBy)
                .totalTestcase(0)
                .timeLimitMs(request.getTimeLimitMs() != null ? request.getTimeLimitMs() : 2000)
                .memoryLimitKb(request.getMemoryLimitKb() != null ? request.getMemoryLimitKb() : 128000)
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : false)
                .score(request.getScore() != null ? java.math.BigDecimal.valueOf(request.getScore()) : new java.math.BigDecimal("100.00"))
                .solutions(request.getSolutions())
                .starterTemplates(templatesJson)
                .totalSubmission(0)
                .totalAccepted(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        ProblemEntity saved = problemRepository.save(problem);

        if (request.getTags() != null) {
            for (String tagName : request.getTags()) {
                if (tagName == null || tagName.isBlank()) continue;
                ProblemTagEntity tag = problemTagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            String slug = tagName.toLowerCase().replace(" ", "-");
                            return problemTagRepository.save(ProblemTagEntity.builder()
                                    .name(tagName)
                                    .slug(slug)
                                    .createdAt(Instant.now())
                                    .updatedAt(Instant.now())
                                    .build());
                        });
                problemTagMappingRepository.save(ProblemTagMappingEntity.builder()
                        .problem(saved)
                        .tag(tag)
                        .build());
            }
        }

        return mapToAdminResponse(saved);
    }

    @Transactional
    public AdminProblemResponse updateAdminProblem(Integer id, AdminProblemRequest request) {
        ProblemEntity problem = problemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND));

        problem.setTitle(request.getTitle());
        problem.setDescription(request.getDescription());
        problem.setInputDescription(request.getInputDescription());
        problem.setOutputDescription(request.getOutputDescription());
        problem.setConstraints(request.getConstraints());
        problem.setExampleInput(request.getExampleInput());
        problem.setExampleOutput(request.getExampleOutput());
        problem.setHint(request.getHint());
        problem.setSolutions(request.getSolutions());

        if (request.getProblemScope() != null) {
            try {
                problem.setProblemScope(ProblemScope.valueOf(request.getProblemScope()));
            } catch (IllegalArgumentException e) {
                // Ignore invalid enum
            }
        }

        if (request.getDifficulty() != null) {
            try {
                problem.setDifficulty(ProblemDifficulty.valueOf(request.getDifficulty()));
            } catch (IllegalArgumentException e) {
                // Ignore invalid enum
            }
        }

        if (request.getTotalTestcases() != null) {
            problem.setTotalTestcase(request.getTotalTestcases());
            problem.setIsActive(request.getTotalTestcases() > 0);
        }
        if (request.getTimeLimitMs() != null) {
            problem.setTimeLimitMs(request.getTimeLimitMs());
        }
        if (request.getMemoryLimitKb() != null) {
            problem.setMemoryLimitKb(request.getMemoryLimitKb());
        }
        if (request.getIsPublic() != null) {
            problem.setIsPublic(request.getIsPublic());
        }
        if (request.getScore() != null) {
            problem.setScore(java.math.BigDecimal.valueOf(request.getScore()));
        }

        if (request.getStarterTemplates() != null) {
            try {
                problem.setStarterTemplates(objectMapper.writeValueAsString(request.getStarterTemplates()));
            } catch (Exception e) {
                log.warn("Failed to serialize starter templates: {}", e.getMessage());
            }
        }

        if (request.getTags() != null) {
            problemTagMappingRepository.deleteByProblemId(problem.getId());
            problemTagMappingRepository.flush();
            java.util.Set<String> uniqueTags = new java.util.LinkedHashSet<>(request.getTags());
            for (String tagName : uniqueTags) {
                if (tagName == null || tagName.isBlank()) continue;
                ProblemTagEntity tag = problemTagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            String slug = tagName.toLowerCase().replace(" ", "-");
                            return problemTagRepository.save(ProblemTagEntity.builder()
                                    .name(tagName)
                                    .slug(slug)
                                    .createdAt(Instant.now())
                                    .updatedAt(Instant.now())
                                    .build());
                        });
                problemTagMappingRepository.save(ProblemTagMappingEntity.builder()
                        .problem(problem)
                        .tag(tag)
                        .build());
            }
        }

        problem.setUpdatedAt(Instant.now());

        ProblemEntity saved = problemRepository.save(problem);
        return mapToAdminResponse(saved);
    }

    @Transactional
    public AdminProblemResponse updateAdminProblemScope(Integer id, String scopeStr) {
        ProblemEntity problem = problemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND));

        try {
            problem.setProblemScope(ProblemScope.valueOf(scopeStr));
            problem.setUpdatedAt(Instant.now());
            ProblemEntity saved = problemRepository.save(problem);
            return mapToAdminResponse(saved);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    @Transactional
    public AdminProblemResponse updateAdminProblemPublicStatus(Integer id, Boolean isPublic) {
        ProblemEntity problem = problemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND));

        problem.setIsPublic(isPublic);
        problem.setUpdatedAt(Instant.now());
        ProblemEntity saved = problemRepository.save(problem);
        return mapToAdminResponse(saved);
    }

    @Transactional
    public AdminProblemResponse activateAdminProblem(Integer id, Integer totalTestcases) {
        ProblemEntity problem = problemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND));

        problem.setTotalTestcase(totalTestcases);
        problem.setIsActive(totalTestcases > 0);
        if (totalTestcases > 0) {
            problem.setIsPublic(true);
        }
        problem.setUpdatedAt(Instant.now());
        ProblemEntity saved = problemRepository.save(problem);
        return mapToAdminResponse(saved);
    }

    @Transactional
    public void deleteAdminProblem(Integer id) {
        ProblemEntity problem = problemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND));

        // Delete comments first due to lack of ON DELETE CASCADE
        problemCommentRepository.deleteByProblemId(problem.getId());

        // Now we can delete the problem itself, other related records (submissions, testcases, tag mappings, etc.) will cascade delete
        problemRepository.delete(problem);
    }

    private AdminProblemResponse mapToAdminResponse(ProblemEntity entity) {
        List<ProblemTagMappingEntity> tagMappings = problemTagMappingRepository.findByProblemId(entity.getId());
        List<String> tags = tagMappings.stream()
                .map(m -> m.getTag().getName())
                .collect(Collectors.toList());

        Map<String, String> templates = null;
        if (entity.getStarterTemplates() != null && !entity.getStarterTemplates().isBlank()) {
            try {
                templates = objectMapper.readValue(entity.getStarterTemplates(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
            } catch (Exception e) {
                log.warn("Failed to parse starter templates for problem {}: {}", entity.getId(), e.getMessage());
            }
        }

        return AdminProblemResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .inputDescription(entity.getInputDescription())
                .outputDescription(entity.getOutputDescription())
                .constraints(entity.getConstraints())
                .exampleInput(entity.getExampleInput())
                .exampleOutput(entity.getExampleOutput())
                .hint(entity.getHint())
                .problemScope(entity.getProblemScope() != null ? entity.getProblemScope().name() : null)
                .difficulty(entity.getDifficulty() != null ? entity.getDifficulty().name() : null)
                .isActive(entity.getIsActive())
                .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null)
                .totalTestcases(entity.getTotalTestcase() != null ? entity.getTotalTestcase() : 0)
                .timeLimitMs(entity.getTimeLimitMs())
                .memoryLimitKb(entity.getMemoryLimitKb())
                .isPublic(entity.getIsPublic())
                .score(entity.getScore() != null ? entity.getScore().doubleValue() : 0.0)
                .solutions(entity.getSolutions())
                .totalSubmissions(entity.getTotalSubmission() != null ? entity.getTotalSubmission() : 0)
                .acceptedSubmissions(entity.getTotalAccepted() != null ? entity.getTotalAccepted() : 0)
                .tags(tags)
                .starterTemplates(templates)
                .build();
    }

    @Transactional(readOnly = true)
    public List<AdminTestcaseResponse> getProblemTestcases(Integer problemId) {
        problemRepository.findById(problemId)
                .orElseThrow(() -> new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND));
        
        List<ProblemTestcaseEntity> tcs = problemTestcaseRepository.findByProblemIdOrderByOrderIndexAsc(problemId);
        return tcs.stream()
                .map(tc -> AdminTestcaseResponse.builder()
                        .id(tc.getId())
                        .problemId(problemId)
                        .inputData(tc.getInputData())
                        .expectedOutput(tc.getExpectedOutput())
                        .orderIndex(tc.getOrderIndex())
                        .token(tc.getToken())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public List<AdminTestcaseResponse> saveProblemTestcases(Integer problemId, List<AdminTestcaseRequest> requests) {
        ProblemEntity problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND));

        // Validate requests
        for (AdminTestcaseRequest req : requests) {
            if (req.getInputData() == null || req.getInputData().trim().isEmpty() ||
                req.getExpectedOutput() == null || req.getExpectedOutput().trim().isEmpty()) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
        }

        // Delete existing
        problemTestcaseRepository.deleteByProblemId(problemId);

        // Map and save new
        List<ProblemTestcaseEntity> newEntities = requests.stream()
                .map(req -> ProblemTestcaseEntity.builder()
                        .problem(problem)
                        .inputData(req.getInputData())
                        .expectedOutput(req.getExpectedOutput())
                        .orderIndex(req.getOrderIndex())
                        .build())
                .collect(Collectors.toList());

        List<ProblemTestcaseEntity> savedEntities = problemTestcaseRepository.saveAll(newEntities);

        // Update problem count and isActive status
        problem.setTotalTestcase(savedEntities.size());
        problem.setIsActive(savedEntities.size() > 0);
        // Xóa logic tự động chuyển sang Public theo yêu cầu
        problemRepository.save(problem);

        return savedEntities.stream()
                .map(tc -> AdminTestcaseResponse.builder()
                        .id(tc.getId())
                        .problemId(problemId)
                        .inputData(tc.getInputData())
                        .expectedOutput(tc.getExpectedOutput())
                        .orderIndex(tc.getOrderIndex())
                        .token(tc.getToken())
                        .build())
                .collect(Collectors.toList());
    }
}

