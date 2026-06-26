package com.swp391.coding_platform.service.ranking;

import com.swp391.coding_platform.dto.response.RankingUserResponse;
import com.swp391.coding_platform.dto.response.UserRankStatsResponse;
import com.swp391.coding_platform.repository.projection.RankingUserProjection;
import com.swp391.coding_platform.repository.user.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RankingService {

    UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<RankingUserResponse> getGlobalRankings(String filter) {
        List<RankingUserProjection> projections = userRepository.getGlobalRankingList();
        List<RankingUserResponse> responses = new ArrayList<>();

        for (int i = 0; i < projections.size(); i++) {
            RankingUserProjection p = projections.get(i);
            double points = p.getPoints() != null ? p.getPoints() : 0.0;
            responses.add(RankingUserResponse.builder()
                    .rank(i + 1)
                    .userId(p.getUserId())
                    .name(p.getDisplayname())
                    .avatar(p.getAvatarurl())
                    .points(points)
                    .build());
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public UserRankStatsResponse getUserRankStats(Integer userId, String filter) {
        Integer userRank = userRepository.getUserRanking(userId);
        if (userRank == null) {
            userRank = 0;
        }

        List<RankingUserProjection> allRankings = userRepository.getGlobalRankingList();
        double points = 0.0;

        for (RankingUserProjection p : allRankings) {
            if (p.getUserId().equals(userId)) {
                points = p.getPoints() != null ? p.getPoints() : 0.0;
                break;
            }
        }

        double pointsToNextRank = 0.0;
        String nextRankUserName = "";

        if (userRank > 1 && userRank - 2 < allRankings.size()) {
            RankingUserProjection nextRankUser = allRankings.get(userRank - 2);
            double nextRankPoints = nextRankUser.getPoints() != null ? nextRankUser.getPoints() : 0.0;
            pointsToNextRank = nextRankPoints - points;
            nextRankUserName = nextRankUser.getDisplayname();
        }

        return UserRankStatsResponse.builder()
                .rank(userRank)
                .points(points)
                .pointsToNextRank(pointsToNextRank)
                .nextRankUserName(nextRankUserName)
                .build();
    }
}
