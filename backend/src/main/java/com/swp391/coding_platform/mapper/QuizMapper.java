package com.swp391.coding_platform.mapper;

import com.swp391.coding_platform.dto.response.*;
import com.swp391.coding_platform.entity.course.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QuizMapper {

    @Mapping(target = "optionId", source = "id")
    QuizOptionResultResponse toQuizOptionResultResponse(QuizOptionEntity option);
}
