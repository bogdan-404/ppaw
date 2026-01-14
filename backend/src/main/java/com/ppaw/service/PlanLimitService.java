package com.ppaw.service;

import com.ppaw.dataaccess.entity.Plan;
import com.ppaw.dataaccess.entity.PlanLimit;
import com.ppaw.dataaccess.repository.PlanLimitRepository;
import com.ppaw.dataaccess.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanLimitService {

    private final PlanLimitRepository planLimitRepository;
    private final PlanRepository planRepository;

    @Transactional
    public PlanLimit createPlanLimit(UUID planId, String key, String value) {
        log.info("Creating plan limit: planId={}, key={}, value={}", planId, key, value);
        Plan plan = planRepository.findById(planId)
            .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

        PlanLimit limit = PlanLimit.builder()
            .plan(plan)
            .key(key)
            .value(value)
            .build();

        limit = planLimitRepository.save(limit);
        log.info("Plan limit created successfully: {}", limit.getId());
        return limit;
    }

    @Transactional
    public PlanLimit updatePlanLimit(UUID limitId, String value) {
        log.info("Updating plan limit: {}", limitId);
        PlanLimit limit = planLimitRepository.findById(limitId)
            .orElseThrow(() -> new IllegalArgumentException("Plan limit not found"));
        limit.setValue(value);
        limit = planLimitRepository.save(limit);
        log.info("Plan limit updated successfully: {}", limitId);
        return limit;
    }

    @Transactional
    public void deletePlanLimit(UUID limitId) {
        log.info("Deleting plan limit: {}", limitId);
        planLimitRepository.deleteById(limitId);
        log.info("Plan limit deleted successfully: {}", limitId);
    }

    public List<PlanLimit> getPlanLimitsByPlanId(UUID planId) {
        return planLimitRepository.findByPlanId(planId);
    }

    public PlanLimit getPlanLimitById(UUID id) {
        return planLimitRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Plan limit not found"));
    }
}
