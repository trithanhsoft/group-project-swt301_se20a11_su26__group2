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
import com.swp391.coding_platform.entity.problem.ProblemVersionEntity;
import com.swp391.coding_platform.entity.problem.ProblemTestcaseEntity;
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
public class AdminProblemService {

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



    public List<AdminProblemResponse> getAdminProblems() {
        return problemRepository.findByProblemScopeIn(
                List.of(ProblemScope.CONTEST, ProblemScope.PRACTICE, ProblemScope.SHARED)
        ).stream()
                .map(this::mapToAdminResponse)
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

        
        boolean finalIsPublic = request.getIsPublic() != null ? request.getIsPublic() : false;
        // Strict check: Newly created problems have 0 testcases, so they cannot be public initially.
        if (finalIsPublic) {
            finalIsPublic = false;
        }

        ProblemEntity problem = ProblemEntity.builder()
                .problemScope(scope)
                .isActive(false)
                .createdBy(createdBy)
                .totalTestcase(0)
                .isPublic(finalIsPublic)
                .build();
        
        com.swp391.coding_platform.entity.problem.ProblemVersionEntity version = com.swp391.coding_platform.entity.problem.ProblemVersionEntity.builder()
                .problem(problem)
                .title(request.getTitle())
                .description(request.getDescription())
                .inputDescription(request.getInputDescription())
                .outputDescription(request.getOutputDescription())
                .constraints(request.getConstraints())
                .exampleInput(request.getExampleInput())
                .exampleOutput(request.getExampleOutput())
                .hint(request.getHint())
                .difficulty(difficulty)
                .timeLimitMs(request.getTimeLimitMs() != null ? request.getTimeLimitMs() : 2000)
                .memoryLimitKb(request.getMemoryLimitKb() != null ? request.getMemoryLimitKb() : 128000)
                .starterTemplates(templatesJson)
                .versionNumber(1)
                .problemScope(scope)
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : false)
                .build();
        
        problem.setCurrentVersion(version);
        problem.getVersions().add(version);


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
                
        if (!problem.getIsActive() && problem.getTotalTestcase() != null && problem.getTotalTestcase() > 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
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
                    .createdAt(Instant.now())
                    .versionNumber(problem.getVersions().stream().mapToInt(com.swp391.coding_platform.entity.problem.ProblemVersionEntity::getVersionNumber).max().orElse(0) + 1)
                    .build();
            targetVersion = problemVersionRepository.save(targetVersion);
            problem.setCurrentVersion(targetVersion);
            problem.getVersions().add(targetVersion);
            
            // Copy testcases from old version to new version
            java.util.List<com.swp391.coding_platform.entity.problem.ProblemTestcaseEntity> oldTestcases = problemTestcaseRepository.findByProblemVersionIdOrderByOrderIndexAsc(problem.getCurrentVersion().getId());
            java.util.List<com.swp391.coding_platform.entity.problem.ProblemTestcaseEntity> newTestcases = new java.util.ArrayList<>();
            for (com.swp391.coding_platform.entity.problem.ProblemTestcaseEntity oldTc : oldTestcases) {
                com.swp391.coding_platform.entity.problem.ProblemTestcaseEntity newTc = com.swp391.coding_platform.entity.problem.ProblemTestcaseEntity.builder()
                        .problemVersion(targetVersion)
                        .inputData(oldTc.getInputData())
                        .expectedOutput(oldTc.getExpectedOutput())
                        .token(java.util.UUID.randomUUID().toString())
                        .orderIndex(oldTc.getOrderIndex())
                        .build();
                newTestcases.add(newTc);
            }
            if (!newTestcases.isEmpty()) {
                problemTestcaseRepository.saveAll(newTestcases);
            }
        }

        targetVersion.setTitle(request.getTitle());
        targetVersion.setDescription(request.getDescription());
        targetVersion.setInputDescription(request.getInputDescription());
        targetVersion.setOutputDescription(request.getOutputDescription());
        targetVersion.setConstraints(request.getConstraints());
        targetVersion.setExampleInput(request.getExampleInput());
        targetVersion.setExampleOutput(request.getExampleOutput());
        targetVersion.setHint(request.getHint());
        targetVersion.setSolutions(request.getSolutions());

        if (request.getProblemScope() != null) {
            try {
                ProblemScope newScope = ProblemScope.valueOf(request.getProblemScope());
                problem.setProblemScope(newScope);
                targetVersion.setProblemScope(newScope);
            } catch (IllegalArgumentException e) {
                // Ignore invalid enum
            }
        }

        if (request.getDifficulty() != null) {
            try {
                targetVersion.setDifficulty(ProblemDifficulty.valueOf(request.getDifficulty()));
            } catch (IllegalArgumentException e) {
                // Ignore invalid enum
            }
        }

        if (request.getTotalTestcases() != null) {
            problem.setTotalTestcase(request.getTotalTestcases());
            problem.setIsActive(request.getTotalTestcases() > 0);
        }
        if (request.getTimeLimitMs() != null) {
            targetVersion.setTimeLimitMs(request.getTimeLimitMs());
        }
        if (request.getMemoryLimitKb() != null) {
            targetVersion.setMemoryLimitKb(request.getMemoryLimitKb());
        }
        if (request.getIsPublic() != null) {
            boolean finalIsPublic = request.getIsPublic();
            if (finalIsPublic && (problem.getTotalTestcase() == null || problem.getTotalTestcase() == 0)) {
                finalIsPublic = false; // Force to false if no testcases
            }
            problem.setIsPublic(finalIsPublic);
            targetVersion.setIsPublic(finalIsPublic);
        }
        if (request.getScore() != null) {
            problem.setScore(java.math.BigDecimal.valueOf(request.getScore()));
        }

        if (request.getStarterTemplates() != null) {
            try {
                targetVersion.setStarterTemplates(objectMapper.writeValueAsString(request.getStarterTemplates()));
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

        if (!problem.getIsActive() && problem.getTotalTestcase() != null && problem.getTotalTestcase() > 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

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

        if (!problem.getIsActive() && problem.getTotalTestcase() != null && problem.getTotalTestcase() > 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (Boolean.TRUE.equals(isPublic) && (problem.getTotalTestcase() == null || problem.getTotalTestcase() == 0)) {
            throw new AppException(ErrorCode.OJ_PROBLEM_MISSING_TESTCASE);
        }

        problem.setIsPublic(isPublic);
        problem.setUpdatedAt(Instant.now());
        ProblemEntity saved = problemRepository.save(problem);
        return mapToAdminResponse(saved);
    }

    @Transactional
    public AdminProblemResponse activateAdminProblem(Integer id, Integer totalTestcases) {
        ProblemEntity problem = problemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND));

        if (!problem.getIsActive() && problem.getTotalTestcase() != null && problem.getTotalTestcase() > 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

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

        if (!problem.getIsActive() && problem.getTotalTestcase() != null && problem.getTotalTestcase() > 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // If the problem has submissions, perform a soft delete to preserve historical data
        long submissionCount = problemSubmissionRepository.countByProblemId(id);
        if (submissionCount > 0) {
            problem.setIsPublic(false);
            problem.setIsActive(false);
            problem.setUpdatedAt(Instant.now());
            problemRepository.save(problem);
            return;
        }

        // Hard delete for problems with no submissions
        // Delete comments first due to lack of ON DELETE CASCADE
        problemCommentRepository.deleteByProblemId(problem.getId());

        // Now we can delete the problem itself, other related records (submissions, testcases, tag mappings, etc.) will cascade delete
        problemRepository.delete(problem);
    }

    @Transactional
    public AdminProblemResponse rollbackAdminProblem(Integer id, Integer versionId) {
        ProblemEntity problem = problemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND));

        if (!problem.getIsActive() && problem.getTotalTestcase() != null && problem.getTotalTestcase() > 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        ProblemVersionEntity targetVersion = problemVersionRepository.findById(versionId)
                .orElseThrow(() -> new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND));

        if (!targetVersion.getProblem().getId().equals(problem.getId())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        ProblemVersionEntity newVersion = ProblemVersionEntity.builder()
                .problem(problem)
                .title(targetVersion.getTitle())
                .description(targetVersion.getDescription())
                .inputDescription(targetVersion.getInputDescription())
                .outputDescription(targetVersion.getOutputDescription())
                .constraints(targetVersion.getConstraints())
                .exampleInput(targetVersion.getExampleInput())
                .exampleOutput(targetVersion.getExampleOutput())
                .hint(targetVersion.getHint())
                .difficulty(targetVersion.getDifficulty())
                .timeLimitMs(targetVersion.getTimeLimitMs())
                .memoryLimitKb(targetVersion.getMemoryLimitKb())
                .problemScope(targetVersion.getProblemScope())
                .isPublic(targetVersion.getIsPublic())
                .solutions(targetVersion.getSolutions())
                .starterTemplates(targetVersion.getStarterTemplates())
                .createdAt(Instant.now())
                .versionNumber(problem.getVersions().stream().mapToInt(com.swp391.coding_platform.entity.problem.ProblemVersionEntity::getVersionNumber).max().orElse(0) + 1)
                .build();

        newVersion = problemVersionRepository.save(newVersion);
        problem.setCurrentVersion(newVersion);
        problem.setUpdatedAt(Instant.now());
        problem.setProblemScope(newVersion.getProblemScope());

        java.util.List<com.swp391.coding_platform.entity.problem.ProblemTestcaseEntity> oldTestcases = problemTestcaseRepository.findByProblemVersionIdOrderByOrderIndexAsc(versionId);
        java.util.List<com.swp391.coding_platform.entity.problem.ProblemTestcaseEntity> newTestcases = new java.util.ArrayList<>();
        
        for (com.swp391.coding_platform.entity.problem.ProblemTestcaseEntity oldTc : oldTestcases) {
            com.swp391.coding_platform.entity.problem.ProblemTestcaseEntity newTc = com.swp391.coding_platform.entity.problem.ProblemTestcaseEntity.builder()
                    .problemVersion(newVersion)
                    .inputData(oldTc.getInputData())
                    .expectedOutput(oldTc.getExpectedOutput())
                    .token(java.util.UUID.randomUUID().toString())
                    .orderIndex(oldTc.getOrderIndex())
                    .build();
            newTestcases.add(newTc);
        }
        
        if (!newTestcases.isEmpty()) {
            problemTestcaseRepository.saveAll(newTestcases);
        }

        ProblemEntity saved = problemRepository.save(problem);
        return mapToAdminResponse(saved);
    }

    private AdminProblemResponse mapToAdminResponse(ProblemEntity entity) {
        List<ProblemTagMappingEntity> tagMappings = problemTagMappingRepository.findByProblemId(entity.getId());
        List<String> tags = tagMappings.stream()
                .map(m -> m.getTag().getName())
                .collect(Collectors.toList());

        Map<String, String> templates = null;
        if (entity.getCurrentVersion().getStarterTemplates() != null && !entity.getCurrentVersion().getStarterTemplates().isBlank()) {
            try {
                templates = objectMapper.readValue(entity.getCurrentVersion().getStarterTemplates(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
            } catch (Exception e) {
                log.warn("Failed to parse starter templates for problem {}: {}", entity.getId(), e.getMessage());
            }
        }

        return AdminProblemResponse.builder()
                .id(entity.getId())
                .title(entity.getCurrentVersion().getTitle())
                .description(entity.getCurrentVersion().getDescription())
                .inputDescription(entity.getCurrentVersion().getInputDescription())
                .outputDescription(entity.getCurrentVersion().getOutputDescription())
                .constraints(entity.getCurrentVersion().getConstraints())
                .exampleInput(entity.getCurrentVersion().getExampleInput())
                .exampleOutput(entity.getCurrentVersion().getExampleOutput())
                .hint(entity.getCurrentVersion().getHint())
                .problemScope(entity.getProblemScope() != null ? entity.getProblemScope().name() : null)
                .difficulty(entity.getCurrentVersion().getDifficulty() != null ? entity.getCurrentVersion().getDifficulty().name() : null)
                .isActive(entity.getIsActive())
                .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null)
                .totalTestcases(entity.getTotalTestcase() != null ? entity.getTotalTestcase() : 0)
                .timeLimitMs(entity.getCurrentVersion().getTimeLimitMs())
                .memoryLimitKb(entity.getCurrentVersion().getMemoryLimitKb())
                .isPublic(entity.getIsPublic())
                .score(entity.getScore() != null ? entity.getScore().doubleValue() : 0.0)
                .solutions(entity.getCurrentVersion().getSolutions())
                .totalSubmissions(entity.getTotalSubmission() != null ? entity.getTotalSubmission() : 0)
                .acceptedSubmissions(entity.getTotalAccepted() != null ? entity.getTotalAccepted() : 0)
                .isDeleted(!entity.getIsActive() && entity.getTotalTestcase() != null && entity.getTotalTestcase() > 0)
                .tags(tags)
                .starterTemplates(templates)
                .build();
    }

    @Transactional(readOnly = true)
    public java.util.List<com.swp391.coding_platform.dto.response.ProblemVersionResponse> getProblemVersions(Integer id) {
        ProblemEntity problem = problemRepository.findById(id)
                .orElseThrow(() -> new com.swp391.coding_platform.exception.AppException(com.swp391.coding_platform.exception.ErrorCode.OJ_PROBLEM_NOT_FOUND));

        return problem.getVersions().stream()
                .map(v -> com.swp391.coding_platform.dto.response.ProblemVersionResponse.builder()
                        .id(v.getId())
                        .problemId(problem.getId())
                        .versionNumber(v.getVersionNumber())
                        .title(v.getTitle())
                        .description(v.getDescription())
                        .inputDescription(v.getInputDescription())
                        .outputDescription(v.getOutputDescription())
                        .constraints(v.getConstraints())
                        .exampleInput(v.getExampleInput())
                        .exampleOutput(v.getExampleOutput())
                        .hint(v.getHint())
                        .difficulty(v.getDifficulty() != null ? v.getDifficulty().name() : null)
                        .timeLimitMs(v.getTimeLimitMs())
                        .memoryLimitKb(v.getMemoryLimitKb())
                        .solutions(v.getSolutions())
                        .createdAt(v.getCreatedAt() != null ? v.getCreatedAt().toString() : null)
                        .testcases(v.getTestcases() != null ? v.getTestcases().stream().map(tc -> com.swp391.coding_platform.dto.response.AdminTestcaseResponse.builder()
                                .id(tc.getId())
                                .problemId(problem.getId())
                                .inputData(tc.getInputData())
                                .expectedOutput(tc.getExpectedOutput())
                                .orderIndex(tc.getOrderIndex())
                                .token(tc.getToken())
                                .build()).collect(java.util.stream.Collectors.toList()) : new java.util.ArrayList<>())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public AdminProblemResponse cloneProblem(Integer problemId, Integer adminId) {
        com.swp391.coding_platform.entity.user.UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        ProblemEntity sourceProblem = problemRepository.findById(problemId)
                .orElseThrow(() -> new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND));
        
        if (!sourceProblem.getIsActive() && sourceProblem.getTotalTestcase() != null && sourceProblem.getTotalTestcase() > 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        ProblemVersionEntity currentVersion = sourceProblem.getCurrentVersion();
        if (currentVersion == null) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION); // Or some logical error
        }

        // 1. Create new ProblemEntity
        ProblemEntity clonedProblem = ProblemEntity.builder()
                .createdBy(admin)
                .isActive(true) // Set to true so it doesn't get flagged as DELETED (since it has testcases)
                .problemScope(sourceProblem.getProblemScope() != null ? sourceProblem.getProblemScope() : currentVersion.getProblemScope())
                .isPublic(false) // Cloned problems are always private by default (Draft)
                .totalTestcase(sourceProblem.getTotalTestcase() != null ? sourceProblem.getTotalTestcase() : 0)
                .build();
        
        // 2. Create new Version for the cloned problem
        ProblemVersionEntity clonedVersion = ProblemVersionEntity.builder()
                .problem(clonedProblem)
                .title(currentVersion.getTitle() + " (Copy)")
                .description(currentVersion.getDescription())
                .inputDescription(currentVersion.getInputDescription())
                .outputDescription(currentVersion.getOutputDescription())
                .constraints(currentVersion.getConstraints())
                .exampleInput(currentVersion.getExampleInput())
                .exampleOutput(currentVersion.getExampleOutput())
                .hint(currentVersion.getHint())
                .difficulty(currentVersion.getDifficulty())
                .timeLimitMs(currentVersion.getTimeLimitMs())
                .memoryLimitKb(currentVersion.getMemoryLimitKb())
                .starterTemplates(currentVersion.getStarterTemplates())
                .versionNumber(1)
                .problemScope(sourceProblem.getProblemScope() != null ? sourceProblem.getProblemScope() : currentVersion.getProblemScope())
                .isPublic(false) // Cloned version is always private by default
                .build();
        
        clonedProblem.setCurrentVersion(clonedVersion);
        if (clonedProblem.getVersions() == null) {
            clonedProblem.setVersions(new java.util.ArrayList<>());
        }
        clonedProblem.getVersions().add(clonedVersion);

        // Save problem and version
        ProblemEntity savedClonedProblem = problemRepository.save(clonedProblem);
        ProblemVersionEntity savedClonedVersion = savedClonedProblem.getCurrentVersion();

        // 3. Clone tags
        List<com.swp391.coding_platform.entity.problem.ProblemTagMappingEntity> sourceTagMaps = problemTagMappingRepository.findByProblemId(problemId);
        for (com.swp391.coding_platform.entity.problem.ProblemTagMappingEntity tagMap : sourceTagMaps) {
            com.swp391.coding_platform.entity.problem.ProblemTagMappingEntity clonedTagMap = com.swp391.coding_platform.entity.problem.ProblemTagMappingEntity.builder()
                    .problem(savedClonedProblem)
                    .tag(tagMap.getTag())
                    .build();
            problemTagMappingRepository.save(clonedTagMap);
        }

        // 4. Clone testcases
        List<ProblemTestcaseEntity> sourceTestcases = problemTestcaseRepository.findByProblemVersionIdOrderByOrderIndexAsc(currentVersion.getId());
        for (ProblemTestcaseEntity tc : sourceTestcases) {
            ProblemTestcaseEntity clonedTc = ProblemTestcaseEntity.builder()
                    .problemVersion(savedClonedVersion)
                    .inputData(tc.getInputData())
                    .expectedOutput(tc.getExpectedOutput())
                    .orderIndex(tc.getOrderIndex())
                    .token(java.util.UUID.randomUUID().toString())
                    .build();
            problemTestcaseRepository.save(clonedTc);
        }

        return mapToAdminResponse(savedClonedProblem);
    }
}