package com.swp391.coding_platform.service.problem;

import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.problem.ProblemTestcaseEntity;
import com.swp391.coding_platform.repository.problem.ProblemCommentRepository;
import com.swp391.coding_platform.repository.problem.ProblemRepository;
import com.swp391.coding_platform.repository.problem.ProblemSubmissionRepository;
import com.swp391.coding_platform.repository.problem.ProblemTagMappingRepository;
import com.swp391.coding_platform.repository.problem.ProblemTagRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import com.swp391.coding_platform.repository.problem.ProblemTestcaseRepository;
import com.swp391.coding_platform.dto.request.AdminTestcaseRequest;
import com.swp391.coding_platform.dto.response.AdminTestcaseResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProblemTestcaseService {

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

    @Transactional(readOnly = true)
    public List<AdminTestcaseResponse> getProblemTestcases(Integer problemId) {
        com.swp391.coding_platform.entity.problem.ProblemEntity problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND));
        
        List<ProblemTestcaseEntity> tcs = problemTestcaseRepository.findByProblemVersionIdOrderByOrderIndexAsc(problem.getCurrentVersion().getId());
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

        if (!problem.getIsActive() && problem.getTotalTestcase() != null && problem.getTotalTestcase() > 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // Validate requests
        for (AdminTestcaseRequest req : requests) {
            if (req.getInputData() == null || req.getInputData().trim().isEmpty() ||
                req.getExpectedOutput() == null || req.getExpectedOutput().trim().isEmpty()) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
        }

        boolean hasSubmissions = problemSubmissionRepository.countByProblemVersionId(problem.getCurrentVersion().getId()) > 0;
        com.swp391.coding_platform.entity.problem.ProblemVersionEntity targetVersion = problem.getCurrentVersion();

        if (hasSubmissions) {
            targetVersion = com.swp391.coding_platform.entity.problem.ProblemVersionEntity.builder()
                    .problem(problem)
                    .title(problem.getCurrentVersion().getTitle())
                    .description(problem.getCurrentVersion().getDescription())
                    .inputDescription(problem.getCurrentVersion().getInputDescription())
                    .outputDescription(problem.getCurrentVersion().getOutputDescription())
                    .constraints(problem.getCurrentVersion().getConstraints())
                    .exampleInput(problem.getCurrentVersion().getExampleInput())
                    .exampleOutput(problem.getCurrentVersion().getExampleOutput())
                    .hint(problem.getCurrentVersion().getHint())
                    .difficulty(problem.getCurrentVersion().getDifficulty())
                    .timeLimitMs(problem.getCurrentVersion().getTimeLimitMs())
                    .memoryLimitKb(problem.getCurrentVersion().getMemoryLimitKb())
                    .problemScope(problem.getCurrentVersion().getProblemScope())
                    .isPublic(problem.getCurrentVersion().getIsPublic())
                    .solutions(problem.getCurrentVersion().getSolutions())
                    .starterTemplates(problem.getCurrentVersion().getStarterTemplates())
                    .createdAt(java.time.Instant.now())
                    .versionNumber(problem.getVersions().stream().mapToInt(com.swp391.coding_platform.entity.problem.ProblemVersionEntity::getVersionNumber).max().orElse(0) + 1)
                    .build();
            targetVersion = problemVersionRepository.save(targetVersion);
            problem.setCurrentVersion(targetVersion);
            problem.getVersions().add(targetVersion);
        } else {
            // Delete existing testcases since we are updating in place
            problemTestcaseRepository.deleteByProblemVersionId(problem.getCurrentVersion().getId());
        }
        
        final com.swp391.coding_platform.entity.problem.ProblemVersionEntity finalTarget = targetVersion;

        // Map and save new
        List<ProblemTestcaseEntity> newEntities = requests.stream()
                .map(req -> ProblemTestcaseEntity.builder()
                        .problemVersion(finalTarget)
                        .inputData(req.getInputData())
                        .expectedOutput(req.getExpectedOutput())
                        .orderIndex(req.getOrderIndex())
                        .token(java.util.UUID.randomUUID().toString())
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
