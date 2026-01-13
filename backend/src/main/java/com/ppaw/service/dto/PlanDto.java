package com.ppaw.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanDto {
    private UUID id;
    private String code;
    private String name;
    private Integer priceCents;
    private String billingPeriod;
    private Boolean isActive;
    private Map<String, String> limits;
}
