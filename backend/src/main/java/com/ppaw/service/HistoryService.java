package com.ppaw.service;

import com.ppaw.dataaccess.entity.SavedWork;
import com.ppaw.dataaccess.repository.SavedWorkRepository;
import com.ppaw.service.dto.SavedWorkDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryService {

    private final SavedWorkRepository savedWorkRepository;

    public List<SavedWorkDto> getHistory(UUID userId) {
        List<SavedWork> works = savedWorkRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
        return works.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteWork(UUID userId, UUID workId) {
        log.info("Deleting saved work: userId={}, workId={}", userId, workId);
        SavedWork work = savedWorkRepository.findByIdAndDeletedAtIsNull(workId)
            .orElseThrow(() -> new IllegalArgumentException("Work not found"));
        
        if (!work.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized");
        }
        
        work.setDeletedAt(LocalDateTime.now());
        savedWorkRepository.save(work);
        log.info("Work logically deleted: {}", workId);
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
