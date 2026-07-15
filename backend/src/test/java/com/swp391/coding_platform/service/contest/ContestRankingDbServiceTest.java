package com.swp391.coding_platform.service.contest;

import com.swp391.coding_platform.dto.message.ContestRankingDbUpdateMessage;
import com.swp391.coding_platform.entity.contest.ContestRankingEntity;
import com.swp391.coding_platform.entity.contest.ContestProblemAttemptEntity;
import com.swp391.coding_platform.entity.contest.ContestEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.repository.contest.ContestProblemAttemptRepository;
import com.swp391.coding_platform.repository.contest.ContestRankingRepository;
import com.swp391.coding_platform.repository.contest.ContestRepository;
import com.swp391.coding_platform.repository.problem.ProblemRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContestRankingDbServiceTest {

    @Mock
    private ContestRankingRepository contestRankingRepository;
    @Mock
    private ContestRepository contestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ContestProblemAttemptRepository contestProblemAttemptRepository;
    @Mock
    private ProblemRepository problemRepository;

    @InjectMocks
    private ContestRankingDbService contestRankingDbService;

    @Test
    void persistRankingToDatabase_ExistingRanking_UpdatesSuccessfully() {
        // Arrange
        ContestRankingDbUpdateMessage message = ContestRankingDbUpdateMessage.builder()
                .contestId(1)
                .userId(10)
                .problemsSolved(2)
                .totalPenaltyMinutes(100)
                .problemId(1001)
                .isSolved(true)
                .solvedAtSeconds(3600)
                .failedAttemptsCount(1)
                .build();

        ContestRankingEntity existingRanking = new ContestRankingEntity();
        when(contestRankingRepository.findByContestIdAndUserId(1, 10))
                .thenReturn(Optional.of(existingRanking));

        ContestProblemAttemptEntity existingAttempt = new ContestProblemAttemptEntity();
        when(contestProblemAttemptRepository.findByContestIdAndUserIdAndProblemId(1, 10, 1001))
                .thenReturn(Optional.of(existingAttempt));

        // Act
        contestRankingDbService.persistRankingToDatabase(message);

        // Assert
        verify(contestRankingRepository).save(existingRanking);
        verify(contestProblemAttemptRepository).save(existingAttempt);
    }

    @Test
    void persistRankingToDatabase_NewRanking_InsertsSuccessfully() {
        // Arrange
        ContestRankingDbUpdateMessage message = ContestRankingDbUpdateMessage.builder()
                .contestId(1)
                .userId(10)
                .problemsSolved(2)
                .totalPenaltyMinutes(100)
                .problemId(1001)
                .isSolved(true)
                .solvedAtSeconds(3600)
                .failedAttemptsCount(1)
                .build();

        when(contestRankingRepository.findByContestIdAndUserId(1, 10))
                .thenReturn(Optional.empty());
        when(contestRepository.getReferenceById(1)).thenReturn(mock(ContestEntity.class));
        when(userRepository.getReferenceById(10)).thenReturn(mock(UserEntity.class));

        when(contestProblemAttemptRepository.findByContestIdAndUserIdAndProblemId(1, 10, 1001))
                .thenReturn(Optional.empty());
        when(problemRepository.getReferenceById(1001)).thenReturn(mock(ProblemEntity.class));

        // Act
        contestRankingDbService.persistRankingToDatabase(message);

        // Assert
        verify(contestRankingRepository).save(any(ContestRankingEntity.class));
        verify(contestProblemAttemptRepository).save(any(ContestProblemAttemptEntity.class));
    }

    @Test
    void persistRankingToDatabase_Exception_LogsErrorAndDoesNotThrow() {
        // Arrange
        ContestRankingDbUpdateMessage message = ContestRankingDbUpdateMessage.builder()
                .contestId(1)
                .userId(10)
                .build();

        when(contestRankingRepository.findByContestIdAndUserId(1, 10))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        contestRankingDbService.persistRankingToDatabase(message);

        // Assert
        verify(contestRankingRepository).findByContestIdAndUserId(1, 10);
        verifyNoMoreInteractions(contestRankingRepository);
    }
}
