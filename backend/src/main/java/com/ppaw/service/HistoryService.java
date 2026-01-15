package com.ppaw.service;

import com.ppaw.dataaccess.entity.SavedWork;
import com.ppaw.dataaccess.repository.SavedWorkRepository;
import com.ppaw.service.dto.SavedWorkDto;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryService {

    private final SavedWorkRepository savedWorkRepository;
    private final CacheManager cacheManager;
    private final EntityManager entityManager;

    @Cacheable(value = "history", key = "#userId")
    @Transactional(readOnly = true)
    public List<SavedWorkDto> getHistory(UUID userId) {
        log.info("Fetching history for user: {}", userId);
        List<SavedWork> works = savedWorkRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId);
        log.info("Found {} history items for user: {}", works.size(), userId);
        return works.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public void softDeleteWork(UUID userId, UUID workId) {
        log.info("Soft deleting saved work: userId={}, workId={}", userId, workId);
        SavedWork work = savedWorkRepository.findByIdAndIsDeletedFalse(workId)
            .orElseThrow(() -> new IllegalArgumentException("Work not found"));
        
        if (!work.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized");
        }
        
        work.setIsDeleted(true);
        savedWorkRepository.save(work);
        entityManager.flush(); // Ensure changes are persisted before evicting cache
        log.info("Work soft deleted: {}", workId);
        
        // Evict cache after delete
        if (cacheManager.getCache("history") != null) {
            cacheManager.getCache("history").evict(userId);
            log.info("Cache evicted for user history: {} (soft delete)", userId);
        }
    }

    @Transactional
    public void hardDeleteWork(UUID userId, UUID workId) {
        log.info("Hard deleting saved work: userId={}, workId={}", userId, workId);
        SavedWork work = savedWorkRepository.findById(workId)
            .orElseThrow(() -> new IllegalArgumentException("Work not found"));
        
        if (!work.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized");
        }
        
        savedWorkRepository.delete(work);
        entityManager.flush(); // Ensure changes are persisted before evicting cache
        log.info("Work hard deleted: {}", workId);
        
        // Evict cache after delete
        if (cacheManager.getCache("history") != null) {
            cacheManager.getCache("history").evict(userId);
            log.info("Cache evicted for user history: {} (hard delete)", userId);
        }
    }

    // For backward compatibility
    @Transactional
    public void deleteWork(UUID userId, UUID workId) {
        softDeleteWork(userId, workId);
    }

    private SavedWorkDto toDto(SavedWork work) {
        String inputExcerpt = work.getInputText().length() > 100 
            ? work.getInputText().substring(0, 100) + "..." 
            : work.getInputText();
        String outputExcerpt = work.getOutputText().length() > 100 
            ? work.getOutputText().substring(0, 100) + "..." 
            : work.getOutputText();

        return SavedWorkDto.builder()
            .id(work.getId())
            .workType(work.getWorkType().name())
            .inputText(inputExcerpt)
            .outputText(outputExcerpt)
            .style(work.getStyle())
            .createdAt(work.getCreatedAt())
            .build();
    }
}
