package com.ppaw.dataaccess.repository;

import com.ppaw.dataaccess.entity.Plan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {
    Optional<Plan> findByCode(String code);
    
    @EntityGraph(attributePaths = {"planLimits"})
    @Query("SELECT p FROM Plan p WHERE p.isActive = true")
    List<Plan> findByIsActiveTrue();
    
    @EntityGraph(attributePaths = {"planLimits"})
    Optional<Plan> findWithLimitsById(UUID id);
    
    @EntityGraph(attributePaths = {"planLimits"})
    Optional<Plan> findWithLimitsByCode(String code);
    
    @Modifying
    @Query("UPDATE Plan p SET p.isActive = false WHERE p.id = :id")
    void deactivatePlan(@Param("id") UUID id);
}
