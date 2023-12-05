package com.example.healthgenie.boundedContext.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchingRequest {

    private LocalDateTime date;
    private String place;
    private Boolean isAccepted;
    private Integer price;
    private String description;
    private String userEmail;
    private String trainerEmail;
}