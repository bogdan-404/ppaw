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
public class SubscriptionDto {
    private UUID id;
    private UUID planId;
    private String planCode;
    private String planName;
    private String status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
