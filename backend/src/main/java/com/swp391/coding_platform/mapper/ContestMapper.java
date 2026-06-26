package com.swp391.coding_platform.mapper;

import com.swp391.coding_platform.dto.response.ContestResponse;
import com.swp391.coding_platform.dto.response.AdminContestResponse;
import com.swp391.coding_platform.dto.response.AdminContestProblemResponse;
import com.swp391.coding_platform.entity.contest.ContestEntity;
import com.swp391.coding_platform.entity.contest.ContestProblemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContestMapper {

    @Mapping(target = "creatorName", source = "createdBy.displayname")
    @Mapping(target = "isPrivate", expression = "java(contestEntity.getPasswordHash() != null && !contestEntity.getPasswordHash().trim().isEmpty())")
    @Mapping(target = "status", ignore = true)
    ContestResponse toContestResponse(ContestEntity contestEntity);

    @Mapping(target = "creatorName", source = "createdBy.displayname")
    @Mapping(target = "isPrivate", expression = "java(contestEntity.getPasswordHash() != null && !contestEntity.getPasswordHash().trim().isEmpty())")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "participantCount", ignore = true)
    @Mapping(target = "problemCount", ignore = true)
    @Mapping(target = "submissionCount", ignore = true)
    @Mapping(target = "averageScore", ignore = true)
    @Mapping(target = "isDeleted", expression = "java(contestEntity.getStatus() == com.swp391.coding_platform.entity.enums.ContestStatus.DELETED)")
    @Mapping(target = "databaseStatus", expression = "java(contestEntity.getStatus() != null ? contestEntity.getStatus().name() : \"PUBLISHED\")")
    AdminContestResponse toAdminContestResponse(ContestEntity contestEntity);

    @Mapping(target = "problemId", source = "problem.id")
    @Mapping(target = "title", source = "problem.title")
    @Mapping(target = "difficulty", expression = "java(contestProblemEntity.getProblem().getDifficulty() != null ? contestProblemEntity.getProblem().getDifficulty().name() : null)")
    @Mapping(target = "score", source = "problem.score")
    AdminContestProblemResponse toAdminContestProblemResponse(ContestProblemEntity contestProblemEntity);
}
