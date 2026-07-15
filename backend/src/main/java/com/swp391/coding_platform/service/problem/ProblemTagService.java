package com.swp391.coding_platform.service.problem;

import com.swp391.coding_platform.repository.problem.ProblemCommentRepository;
import com.swp391.coding_platform.repository.problem.ProblemRepository;
import com.swp391.coding_platform.repository.problem.ProblemSubmissionRepository;
import com.swp391.coding_platform.repository.problem.ProblemTagMappingRepository;
import com.swp391.coding_platform.repository.problem.ProblemTagRepository;
import com.swp391.coding_platform.entity.problem.ProblemTagEntity;
import com.swp391.coding_platform.dto.response.ProblemTagResponse;
import com.swp391.coding_platform.repository.user.UserRepository;
import com.swp391.coding_platform.repository.problem.ProblemTestcaseRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProblemTagService {

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
}
