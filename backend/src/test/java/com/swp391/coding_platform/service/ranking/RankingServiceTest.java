package com.swp391.coding_platform.service.ranking;

import com.swp391.coding_platform.dto.response.RankingUserResponse;
import com.swp391.coding_platform.dto.response.UserRankStatsResponse;
import com.swp391.coding_platform.repository.projection.RankingUserProjection;
import com.swp391.coding_platform.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RankingService rankingService;

    private RankingUserProjection mockProjection1;
    private RankingUserProjection mockProjection2;

    @BeforeEach
    void setUp() {
        mockProjection1 = new RankingUserProjection() {
            @Override
            public Integer getUserId() { return 1; }
            @Override
            public String getDisplayname() { return "User One"; }
            @Override
            public String getAvatarurl() { return "avatar1.png"; }
            @Override
            public Double getPoints() { return 150.0; }
        };

        mockProjection2 = new RankingUserProjection() {
            @Override
            public Integer getUserId() { return 2; }
            @Override
            public String getDisplayname() { return "User Two"; }
            @Override
            public String getAvatarurl() { return "avatar2.png"; }
            @Override
            public Double getPoints() { return 100.0; }
        };
    }

    @Test
    void getGlobalRankings_shouldReturnRankedList() {
        when(userRepository.getGlobalRankingList()).thenReturn(Arrays.asList(mockProjection1, mockProjection2));

        List<RankingUserResponse> results = rankingService.getGlobalRankings("all");

        assertNotNull(results);
        assertEquals(2, results.size());
        
        assertEquals(1, results.get(0).getRank());
        assertEquals(1, results.get(0).getUserId());
        assertEquals("User One", results.get(0).getName());
        assertEquals(150.0, results.get(0).getPoints());

        assertEquals(2, results.get(1).getRank());
        assertEquals(2, results.get(1).getUserId());
        assertEquals(100.0, results.get(1).getPoints());
        
        verify(userRepository, times(1)).getGlobalRankingList();
    }

    @Test
    void getUserRankStats_shouldReturnStatsForUser() {
        when(userRepository.getUserRanking(2)).thenReturn(2);
        when(userRepository.getGlobalRankingList()).thenReturn(Arrays.asList(mockProjection1, mockProjection2));

        UserRankStatsResponse response = rankingService.getUserRankStats(2, "all");

        assertNotNull(response);
        assertEquals(2, response.getRank());
        assertEquals(100.0, response.getPoints());
        assertEquals(50.0, response.getPointsToNextRank());
        assertEquals("User One", response.getNextRankUserName());
        
        verify(userRepository, times(1)).getUserRanking(2);
        verify(userRepository, times(1)).getGlobalRankingList();
    }
    
    @Test
    void getUserRankStats_shouldHandleRankOneUser() {
        when(userRepository.getUserRanking(1)).thenReturn(1);
        when(userRepository.getGlobalRankingList()).thenReturn(Arrays.asList(mockProjection1, mockProjection2));

        UserRankStatsResponse response = rankingService.getUserRankStats(1, "all");

        assertNotNull(response);
        assertEquals(1, response.getRank());
        assertEquals(150.0, response.getPoints());
        assertEquals(0.0, response.getPointsToNextRank());
        assertEquals("", response.getNextRankUserName());
    }

    @Test
    void getUserRankStats_shouldHandleUnrankedUser() {
        when(userRepository.getUserRanking(99)).thenReturn(null);
        when(userRepository.getGlobalRankingList()).thenReturn(Arrays.asList(mockProjection1, mockProjection2));

        UserRankStatsResponse response = rankingService.getUserRankStats(99, "all");

        assertNotNull(response);
        assertEquals(0, response.getRank());
        assertEquals(0.0, response.getPoints());
        assertEquals(0.0, response.getPointsToNextRank());
        assertEquals("", response.getNextRankUserName());
    }
}
