package com.ppaw.dataaccess.repository;

import com.ppaw.dataaccess.entity.PlanLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanLimitRepository extends JpaRepository<PlanLimit, UUID> {
    List<PlanLimit> findByPlanId(UUID planId);
    
    Optional<PlanLimit> findByPlanIdAndKey(UUID planId, String key);
    
    void deleteByPlanId(UUID planId);
}
