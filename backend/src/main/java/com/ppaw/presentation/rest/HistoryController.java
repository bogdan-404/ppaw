package com.ppaw.presentation.rest;

import com.ppaw.service.HistoryService;
import com.ppaw.service.dto.SavedWorkDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("api")
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public ResponseEntity<List<SavedWorkDto>> getHistory(@RequestHeader("X-USER-ID") String userIdHeader) {
        UUID userId = UUID.fromString(userIdHeader);
        return ResponseEntity.ok(historyService.getHistory(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWork(
            @RequestHeader("X-USER-ID") String userIdHeader,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(userIdHeader);
        historyService.deleteWork(userId, id);
        return ResponseEntity.ok().build();
    }
}
