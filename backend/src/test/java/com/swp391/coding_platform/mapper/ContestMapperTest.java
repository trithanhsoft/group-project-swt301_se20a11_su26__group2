package com.swp391.coding_platform.mapper;

import com.swp391.coding_platform.dto.response.AdminContestProblemResponse;
import com.swp391.coding_platform.dto.response.AdminContestResponse;
import com.swp391.coding_platform.dto.response.ContestResponse;
import com.swp391.coding_platform.entity.contest.ContestEntity;
import com.swp391.coding_platform.entity.contest.ContestProblemEntity;
import com.swp391.coding_platform.entity.enums.ContestStatus;
import com.swp391.coding_platform.entity.enums.ProblemDifficulty;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.problem.ProblemVersionEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContestMapperTest {

    private final ContestMapper mapper = new ContestMapperImpl();

    @Test
    void toContestResponse() {
        ContestEntity entity = new ContestEntity();
        UserEntity creator = new UserEntity();
        creator.setDisplayname("Admin User");
        entity.setCreatedBy(creator);
        entity.setPasswordHash(" secret ");

        ContestResponse response = mapper.toContestResponse(entity);

        assertEquals("Admin User", response.getCreatorName());
        assertTrue(response.getIsPrivate());
        assertNull(response.getStatus()); // ignored
    }

    @Test
    void toContestResponse_NotPrivate() {
        ContestEntity entity = new ContestEntity();
        entity.setPasswordHash("   ");

        ContestResponse response = mapper.toContestResponse(entity);
        assertFalse(response.getIsPrivate());
    }

    @Test
    void toAdminContestResponse() {
        ContestEntity entity = new ContestEntity();
        entity.setStatus(ContestStatus.DELETED);

        AdminContestResponse response = mapper.toAdminContestResponse(entity);
        assertTrue(response.getIsDeleted());
        assertEquals("DELETED", response.getDatabaseStatus());
        assertFalse(response.getIsPrivate());
    }

    @Test
    void toAdminContestResponse_NullStatus() {
        ContestEntity entity = new ContestEntity();
        entity.setStatus(null);

        AdminContestResponse response = mapper.toAdminContestResponse(entity);
        assertFalse(response.getIsDeleted());
        assertEquals("PUBLISHED", response.getDatabaseStatus());
    }

    @Test
    void toAdminContestProblemResponse() {
        ContestProblemEntity entity = new ContestProblemEntity();
        ProblemEntity problem = new ProblemEntity();
        problem.setId(10);
        problem.setScore(BigDecimal.valueOf(100.0));
        entity.setProblem(problem);

        ProblemVersionEntity version = new ProblemVersionEntity();
        version.setTitle("Hard Problem");
        version.setDifficulty(ProblemDifficulty.HARD);
        entity.setProblemVersion(version);

        AdminContestProblemResponse response = mapper.toAdminContestProblemResponse(entity);
        assertEquals(10, response.getProblemId());
        assertEquals("Hard Problem", response.getTitle());
        assertEquals("HARD", response.getDifficulty());
        assertEquals(100.0, response.getScore());
    }

    @Test
    void toAdminContestProblemResponse_NullDifficulty() {
        ContestProblemEntity entity = new ContestProblemEntity();
        ProblemEntity problem = new ProblemEntity();
        entity.setProblem(problem);
        
        ProblemVersionEntity version = new ProblemVersionEntity();
        entity.setProblemVersion(version);

        AdminContestProblemResponse response = mapper.toAdminContestProblemResponse(entity);
        assertEquals("MEDIUM", response.getDifficulty());
    }
}
