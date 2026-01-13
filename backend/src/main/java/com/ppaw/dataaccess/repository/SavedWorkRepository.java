package com.ppaw.dataaccess.repository;

import com.ppaw.dataaccess.entity.SavedWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavedWorkRepository extends JpaRepository<SavedWork, UUID> {
    List<SavedWork> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId);
    
    Optional<SavedWork> findByIdAndDeletedAtIsNull(UUID id);
}
