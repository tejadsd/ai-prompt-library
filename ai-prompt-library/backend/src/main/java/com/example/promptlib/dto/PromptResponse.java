package com.example.promptlib.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptResponse {
    private Long id;
    private String category;
    private String promptTemplate;
    private String inputPrompt;
    private String outputResponse;
    private String reviewNotes;
    private Integer version;
    private String promptKey;
    private Integer rating;
    private LocalDateTime createdAt;
}
