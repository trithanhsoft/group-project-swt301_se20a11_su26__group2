package com.swp391.coding_platform.service.problem;

import com.swp391.coding_platform.dto.request.SubmitRequest;
import com.swp391.coding_platform.dto.response.ProblemSubmissionResponse;
import com.swp391.coding_platform.dto.response.SubmitResponse;
import com.swp391.coding_platform.entity.enums.OjVerdict;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.problem.ProblemSubmissionDetailEntity;
import com.swp391.coding_platform.entity.problem.ProblemSubmissionEntity;
import com.swp391.coding_platform.entity.problem.ProblemTestcaseEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.repository.problem.ProblemRepository;
import com.swp391.coding_platform.repository.problem.ProblemSubmissionDetailRepository;
import com.swp391.coding_platform.repository.problem.ProblemSubmissionRepository;
import com.swp391.coding_platform.repository.problem.ProblemTestcaseRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProblemSubmissionService {

    ProblemSubmissionRepository problemSubmissionRepository;
    ProblemRepository problemRepository;

    public List<ProblemSubmissionResponse> getSubmissions(Integer problemId, Integer userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        ProblemEntity problem = problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(problemId)
                .orElseThrow(() -> new AppException(ErrorCode.OJ_PROBLEM_NOT_FOUND));

        List<ProblemSubmissionEntity> subs = problemSubmissionRepository.findByUserIdAndProblemId(userId.intValue(),
                problemId);
        if (subs.isEmpty()) {
            return Collections.emptyList();
        }

        List<ProblemSubmissionEntity> sortedSubs = new ArrayList<>(subs);
        sortedSubs.sort((a, b) -> b.getSubmittedAt().compareTo(a.getSubmittedAt()));

        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(java.time.ZoneId.systemDefault());

        List<ProblemSubmissionResponse> submissionResponses = new ArrayList<>();

        for (ProblemSubmissionEntity s : sortedSubs) {
            String subStatus = s.getVerdict() == OjVerdict.ACCEPTED ? "Accepted"
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
                default: langStr = "Java"; break;
            }

            String runtimeStr = s.getExecutionTime() != null ? String.format(Locale.US, "%.1f ms", (double) s.getExecutionTime())
                    : "N/A";
            String memoryStr = s.getMemoryUsed() != null
                    ? String.format(Locale.US, "%.1f MB", s.getMemoryUsed() / 1024.0)
                    : "N/A";
            String timeStr = formatter.format(s.getSubmittedAt());
            String statusClass = s.getVerdict() == OjVerdict.ACCEPTED ? "text-brand-green" : "text-red-600";

            submissionResponses.add(ProblemSubmissionResponse.builder()
                    .status(subStatus)
                    .lang(langStr)
                    .runtime(runtimeStr)
                    .memory(memoryStr)
                    .time(timeStr)
                    .statusClass(statusClass)
                    .build());
        }

        return submissionResponses;
    }


}
