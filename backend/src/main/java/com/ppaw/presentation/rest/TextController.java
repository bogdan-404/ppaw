package com.ppaw.presentation.rest;

import com.ppaw.service.TextService;
import com.ppaw.service.dto.TextRequest;
import com.ppaw.service.dto.TextResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/text")
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("api")
public class TextController {

    private final TextService textService;

    @PostMapping("/summarize")
    public ResponseEntity<TextResponse> summarize(
            @RequestHeader("X-USER-ID") String userIdHeader,
            @Valid @RequestBody TextRequest request) {
        try {
            UUID userId = UUID.fromString(userIdHeader);
            return ResponseEntity.ok(textService.summarize(userId, request));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/rewrite")
    public ResponseEntity<TextResponse> rewrite(
            @RequestHeader("X-USER-ID") String userIdHeader,
            @Valid @RequestBody TextRequest request) {
        try {
            UUID userId = UUID.fromString(userIdHeader);
            return ResponseEntity.ok(textService.rewrite(userId, request));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
