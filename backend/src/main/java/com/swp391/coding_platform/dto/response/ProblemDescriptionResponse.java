package com.swp391.coding_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProblemDescriptionResponse {
    Integer id;
    String title;
    String difficulty;
    String description;
    String inputDescription;
    String outputDescription;
    String constraints;
    String exampleInput;
    String exampleOutput;
    String hint;
    List<String> tags;
    Map<String, String> templates;
    String status;
    String acceptance;
    Integer totalSolved;
    
    @com.fasterxml.jackson.annotation.JsonProperty("source_code")
    String sourceCode;
    
    @com.fasterxml.jackson.annotation.JsonProperty("language_id")
    Integer languageId;
}
