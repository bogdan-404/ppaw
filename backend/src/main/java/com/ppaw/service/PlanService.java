package com.ppaw.service;

import com.ppaw.dataaccess.entity.Plan;
import com.ppaw.dataaccess.entity.PlanLimit;
import com.ppaw.dataaccess.repository.PlanRepository;
import com.ppaw.service.dto.PlanDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanService {

    private final PlanRepository planRepository;

    @Transactional(readOnly = true)
    public List<PlanDto> getAllActivePlans() {
        log.info("Fetching all active plans from database");
        List<Plan> plans = planRepository.findByIsActiveTrue();
        log.info("Found {} active plans", plans.size());
        return plans.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlanDto getPlanByCode(String code) {
        log.info("Fetching plan by code: {}", code);
        Plan plan = planRepository.findWithLimitsByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + code));
        return toDto(plan);
    }

    public Plan getPlanEntityByCode(String code) {
        return planRepository.findWithLimitsByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + code));
    }

    @Transactional
    public PlanDto createPlan(PlanDto dto) {
        log.info("Creating new plan: {}", dto.getCode());
        Plan plan = Plan.builder()
            .code(dto.getCode())
            .name(dto.getName())
            .priceCents(dto.getPriceCents())
            .billingPeriod(dto.getBillingPeriod())
            .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
            .build();
        plan = planRepository.save(plan);
        log.info("Plan created successfully: {}", plan.getId());
        return toDto(plan);
    }

    @Transactional
    public PlanDto updatePlan(UUID id, PlanDto dto) {
        log.info("Updating plan: {}, isActive: {}", id, dto.getIsActive());
        Plan plan = planRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Plan not found"));
        plan.setCode(dto.getCode());
        plan.setName(dto.getName());
        plan.setPriceCents(dto.getPriceCents());
        plan.setBillingPeriod(dto.getBillingPeriod());
        plan.setIsActive(dto.getIsActive());
        plan = planRepository.save(plan);
        log.info("Plan updated successfully: {}, isActive: {}", plan.getId(), plan.getIsActive());
        return toDto(plan);
    }

    @Transactional
    public void softDeletePlan(UUID id) {
        log.info("Soft deleting plan: {}", id);
        planRepository.deactivatePlan(id);
        planRepository.flush();
        log.info("Plan soft deleted (deactivated) successfully: {}", id);
    }

    @Transactional
    public void hardDeletePlan(UUID id) {
        log.info("Hard deleting plan: {}", id);
        planRepository.deleteById(id);
        log.info("Plan hard deleted successfully: {}", id);
    }

    private PlanDto toDto(Plan plan) {
        Map<String, String> limits = new HashMap<>();
        if (plan.getPlanLimits() != null) {
            limits = plan.getPlanLimits().stream()
                .collect(Collectors.toMap(PlanLimit::getKey, PlanLimit::getValue));
        }

        return PlanDto.builder()
            .id(plan.getId())
            .code(plan.getCode())
            .name(plan.getName())
            .priceCents(plan.getPriceCents())
            .billingPeriod(plan.getBillingPeriod())
            .isActive(plan.getIsActive())
            .limits(limits)
            .build();
    }

    public List<Plan> getAllPlansForAdmin() {
        return planRepository.findAll();
    }

    public Plan getPlanById(UUID id) {
        return planRepository.findWithLimitsById(id)
            .orElseThrow(() -> new IllegalArgumentException("Plan not found"));
    }
}
