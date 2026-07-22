package com.example.promptlib.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptRequest {
    private String category;
    private String promptTemplate;
    private String inputPrompt;
    private String promptKey; // Optional, passed if re-running or versioning an existing prompt
}
