package com.swp391.coding_platform.service.problem;

import com.swp391.coding_platform.dto.request.CreateCommentRequest;
import com.swp391.coding_platform.dto.response.ProblemCommentResponse;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.problem.ProblemCommentEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProblemCommentService {

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

    public List<ProblemCommentResponse> getComments(Integer problemId) {
        problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(problemId)
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

        ProblemEntity problem = problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(problemId)
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
}
