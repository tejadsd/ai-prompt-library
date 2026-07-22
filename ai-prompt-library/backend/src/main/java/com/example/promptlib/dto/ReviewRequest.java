package com.example.promptlib.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    private String reviewNotes;
    private Integer rating; // Optional rating update (e.g. 1, -1, or null)
}
