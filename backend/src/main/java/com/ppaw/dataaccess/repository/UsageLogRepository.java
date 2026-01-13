package com.ppaw.dataaccess.repository;

import com.ppaw.dataaccess.entity.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface UsageLogRepository extends JpaRepository<UsageLog, UUID> {
    long countByUser_IdAndCreatedAtAfter(UUID userId, LocalDateTime after);
    
    @Query("SELECT COUNT(ul) FROM UsageLog ul WHERE ul.user.id = :userId AND ul.createdAt >= :after")
    long countByUserIdAndCreatedAtAfterQuery(@Param("userId") UUID userId, @Param("after") LocalDateTime after);
}
