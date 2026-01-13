package com.ppaw.presentation.rest;

import com.ppaw.service.SubscriptionService;
import com.ppaw.service.dto.SubscriptionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("api")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<SubscriptionDto> getCurrentSubscription(@RequestHeader("X-USER-ID") String userIdHeader) {
        UUID userId = UUID.fromString(userIdHeader);
        return ResponseEntity.ok(subscriptionService.getCurrentSubscription(userId));
    }

    @PostMapping("/pay")
    public ResponseEntity<SubscriptionDto> payForPlan(
            @RequestHeader("X-USER-ID") String userIdHeader,
            @RequestBody Map<String, String> request) {
        UUID userId = UUID.fromString(userIdHeader);
        String planCode = request.get("planCode");
        return ResponseEntity.ok(subscriptionService.payForPlan(userId, planCode));
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelSubscription(@RequestHeader("X-USER-ID") String userIdHeader) {
        UUID userId = UUID.fromString(userIdHeader);
        subscriptionService.cancelSubscription(userId);
        return ResponseEntity.ok().build();
    }
}
