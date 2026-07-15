package com.swp391.coding_platform.service.problem;

import com.swp391.coding_platform.dto.response.ProblemDescriptionResponse;
import com.swp391.coding_platform.dto.response.ProblemListItemResponse;
import com.swp391.coding_platform.dto.response.ProblemSolutionResponse;
import com.swp391.coding_platform.entity.enums.OjVerdict;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.entity.enums.ProblemScope;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.problem.ProblemSubmissionEntity;
import com.swp391.coding_platform.entity.problem.ProblemTagMappingEntity;
import com.swp391.coding_platform.repository.problem.ProblemCommentRepository;
import com.swp391.coding_platform.repository.problem.ProblemRepository;
import com.swp391.coding_platform.repository.problem.ProblemSubmissionRepository;
import com.swp391.coding_platform.repository.problem.ProblemTagMappingRepository;
import com.swp391.coding_platform.repository.problem.ProblemTagRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import com.swp391.coding_platform.repository.problem.ProblemTestcaseRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProblemService {

    ProblemRepository problemRepository;
    ProblemTagMappingRepository problemTagMappingRepository;
    ProblemSubmissionRepository problemSubmissionRepository;
    ProblemCommentRepository problemCommentRepository;
    UserRepository userRepository;
    ProblemTestcaseRepository problemTestcaseRepository;
    ProblemTagRepository problemTagRepository;
    com.swp391.coding_platform.repository.problem.ProblemVersionRepository problemVersionRepository;

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
            if (problem.getCurrentVersion().getDifficulty() != null) {
                String name = problem.getCurrentVersion().getDifficulty().name();
                difficultyStr = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            }

            return ProblemListItemResponse.builder()
                    .id(problem.getId())
                    .title(problem.getCurrentVersion().getTitle())
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
        ProblemEntity problem = problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND));

        List<ProblemTagMappingEntity> mappings = problemTagMappingRepository.findByProblemId(id);
        List<String> tags = mappings.stream().map(m -> m.getTag().getName()).toList();

        Map<String, String> templates = new HashMap<>();
        if (problem.getCurrentVersion().getStarterTemplates() != null && !problem.getCurrentVersion().getStarterTemplates().isBlank()) {
            try {
                templates = objectMapper.readValue(problem.getCurrentVersion().getStarterTemplates(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
            } catch (Exception e) {
                log.warn("Failed to parse starter templates for problem {}: {}", id, e.getMessage());
            }
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

        return ProblemDescriptionResponse.builder()
                .id(problem.getId())
                .title(problem.getCurrentVersion().getTitle())
                .difficulty(difficultyStr)
                .description(problem.getCurrentVersion().getDescription())
                .inputDescription(problem.getCurrentVersion().getInputDescription())
                .outputDescription(problem.getCurrentVersion().getOutputDescription())
                .constraints(problem.getCurrentVersion().getConstraints())
                .exampleInput(problem.getCurrentVersion().getExampleInput())
                .exampleOutput(problem.getCurrentVersion().getExampleOutput())
                .hint(problem.getCurrentVersion().getHint())
                .tags(tags)
                .templates(templates)
                .status(status)
                .acceptance(acceptance)
                .totalSolved(totalSolved)
                .sourceCode(sourceCode)
                .languageId(languageId)
                .build();
    }

    public ProblemSolutionResponse getProblemSolution(Integer id, Integer userId) {
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        ProblemEntity problem = problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND));

        // Check if the user has solved this problem
        List<ProblemSubmissionEntity> submissions = problemSubmissionRepository.findByUserIdAndProblemId(userId.intValue(), id);
        boolean solved = submissions.stream().anyMatch(s -> s.getVerdict() == OjVerdict.ACCEPTED);

        if (!solved) {
            throw new AppException(ErrorCode.OJ_SOLUTION_LOCKED);
        }

        String solutionCode = problem.getCurrentVersion().getSolutions();
        if (solutionCode == null || solutionCode.isBlank()) {
            solutionCode = "// An official solution for this problem is not available yet.";
        }

        return ProblemSolutionResponse.builder()
                .problemId(problem.getId())
                .title(problem.getCurrentVersion().getTitle())
                .solutionCode(solutionCode)
                .build();
    }

}
