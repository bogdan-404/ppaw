package com.ppaw.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedWorkDto {
    private UUID id;
    private String workType;
    private String inputText;
    private String outputText;
    private String style;
    private LocalDateTime createdAt;
}
