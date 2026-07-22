package com.example.promptlib.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "prompt_executions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category; // "Code", "Documentation", "Testing", "Design"

    @Column(name = "prompt_template")
    private String promptTemplate; // Template name, e.g., "Generate REST API" or "Custom Prompt"

    @Column(name = "input_prompt", columnDefinition = "TEXT", nullable = false)
    private String inputPrompt;

    @Column(name = "output_response", columnDefinition = "TEXT")
    private String outputResponse;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @Column(nullable = false)
    private Integer version; // Version count for this specific promptKey

    @Column(name = "prompt_key", nullable = false)
    private String promptKey; // Grouping identifier (e.g., md5 of prompt, or a clean slug)

    private Integer rating; // e.g., 1 for positive, -1 for negative, null for unrated

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (version == null) {
            version = 1;
        }
    }
}
