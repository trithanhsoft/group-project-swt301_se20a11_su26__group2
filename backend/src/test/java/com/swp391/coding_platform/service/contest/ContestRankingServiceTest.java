package com.swp391.coding_platform.service.contest;

import com.swp391.coding_platform.dto.message.ContestRankingDbUpdateMessage;
import com.swp391.coding_platform.dto.response.ContestScoreboardResponse;
import com.swp391.coding_platform.entity.contest.ContestEntity;
import com.swp391.coding_platform.entity.contest.ContestProblemEntity;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.event.SubmissionJudgedEvent;
import com.swp391.coding_platform.repository.contest.ContestProblemRepository;
import com.swp391.coding_platform.repository.contest.ContestRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContestRankingServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ContestRepository contestRepository;
    @Mock
    private ContestProblemRepository contestProblemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    private ContestRankingService contestRankingService;

    @Test
    void updateContestRanking_ContestNotFound_ReturnsNull() {
        SubmissionJudgedEvent event = SubmissionJudgedEvent.builder()
                .contestId(1)
                .userId(10)
                .problemId(1001)
                .verdict("ACCEPTED")
                .submitTime(Instant.now())
                .build();

        when(contestRepository.findById(1)).thenReturn(Optional.empty());

        ContestScoreboardResponse response = contestRankingService.updateContestRanking(event);

        assertNull(response);
    }

    @Test
    void updateContestRanking_HappyPath_AcceptedSubmission() {
        SubmissionJudgedEvent event = SubmissionJudgedEvent.builder()
                .contestId(1)
                .userId(10)
                .problemId(1001)
                .verdict("ACCEPTED")
                .submitTime(Instant.now().plusSeconds(600))
                .build();

        ContestEntity contest = new ContestEntity();
        contest.setId(1);
        contest.setStartTime(Instant.now());
        contest.setEndTime(Instant.now().plusSeconds(3600));
        when(contestRepository.findById(1)).thenReturn(Optional.of(contest));

        when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);

        // First call returns null, second call returns updated status
        when(hashOperations.get("contest:participant:1:10:live", "problem_1001")).thenReturn(null, "1:0:600");
        
        ProblemEntity problem = new ProblemEntity();
        problem.setId(1001);
        ContestProblemEntity cp = new ContestProblemEntity();
        cp.setProblem(problem);
        cp.setOrderIndex(0);
        when(contestProblemRepository.findByContestIdWithProblem(1)).thenReturn(List.of(cp));

        // (Mock removed because it's merged into the thenReturn above)
        
        // Mock getScoreboard internal call
        when(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong())).thenReturn(Collections.emptySet());

        // Act
        ContestScoreboardResponse response = contestRankingService.updateContestRanking(event);

        // Assert
        verify(hashOperations).put(eq("contest:participant:1:10:live"), eq("problem_1001"), anyString());
        verify(zSetOperations).add(eq("contest:scoreboard:1:live"), eq("10"), anyDouble());
        verify(rabbitTemplate).convertAndSend(anyString(), any(ContestRankingDbUpdateMessage.class));
        assertNotNull(response); 
    }

    @Test
    void getScoreboard_HappyPath() {
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        
        // Mock contest active
        ContestEntity contest = new ContestEntity();
        contest.setEndTime(Instant.now().plusSeconds(3600));

        Set<ZSetOperations.TypedTuple<String>> rankedMembers = new LinkedHashSet<>();
        ZSetOperations.TypedTuple<String> tuple = mock(ZSetOperations.TypedTuple.class);
        when(tuple.getValue()).thenReturn("10");
        when(tuple.getScore()).thenReturn((1.0 * 1e10) + (1_000_000_000L - 600)); // 1 solved, 10 min penalty
        rankedMembers.add(tuple);

        when(zSetOperations.reverseRangeWithScores("contest:scoreboard:1:live", 0L, -1L)).thenReturn(rankedMembers);

        ProblemEntity problem = new ProblemEntity();
        problem.setId(1001);
        ContestProblemEntity cp = new ContestProblemEntity();
        cp.setProblem(problem);
        cp.setOrderIndex(0);
        
        List<ContestProblemEntity> cpList = new ArrayList<>();
        cpList.add(cp);
        when(contestProblemRepository.findByContestIdWithProblem(1)).thenReturn(cpList);

        UserEntity user = new UserEntity();
        user.setId(10);
        user.setUsername("testuser");
        user.setDisplayname("Test User");
        when(userRepository.findAllById(List.of(10))).thenReturn(List.of(user));

        when(hashOperations.get("contest:participant:1:10:live", "problem_1001")).thenReturn("1:0:600");
        when(valueOperations.get("contest:first_solve:1:1001")).thenReturn("10"); // Test first solve status

        ContestScoreboardResponse response = contestRankingService.getScoreboard(1, true);

        assertNotNull(response);
        assertEquals(1, response.getContestId());
        assertEquals(1, response.getRows().size());
        assertEquals("testuser", response.getRows().get(0).getName());
        assertEquals(1, response.getRows().get(0).getSolved());
        assertEquals("first_solve", response.getRows().get(0).getSubmissions().get("A").getStatus());
    }

    @Test
    void getScoreboard_Empty() {
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong())).thenReturn(Collections.emptySet());

        ContestScoreboardResponse response = contestRankingService.getScoreboard(1, true);

        assertNotNull(response);
        assertTrue(response.getRows().isEmpty());
    }
}
