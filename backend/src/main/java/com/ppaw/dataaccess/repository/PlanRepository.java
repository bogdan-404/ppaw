package com.ppaw.dataaccess.repository;

import com.ppaw.dataaccess.entity.Plan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {
    Optional<Plan> findByCode(String code);
    
    @EntityGraph(attributePaths = {"planLimits"})
    List<Plan> findByIsActiveTrue();
    
    @EntityGraph(attributePaths = {"planLimits"})
    Optional<Plan> findWithLimitsById(UUID id);
    
    @EntityGraph(attributePaths = {"planLimits"})
    Optional<Plan> findWithLimitsByCode(String code);
}
