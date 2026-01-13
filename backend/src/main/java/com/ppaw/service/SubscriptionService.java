package com.ppaw.service;

import com.ppaw.dataaccess.entity.Plan;
import com.ppaw.dataaccess.entity.Subscription;
import com.ppaw.dataaccess.entity.Subscription.SubscriptionStatus;
import com.ppaw.dataaccess.entity.User;
import com.ppaw.dataaccess.repository.PlanRepository;
import com.ppaw.dataaccess.repository.SubscriptionRepository;
import com.ppaw.service.dto.SubscriptionDto;
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
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final PlanService planService;
    private final AuthService authService;

    public SubscriptionDto getCurrentSubscription(UUID userId) {
        Subscription subscription = subscriptionRepository.findFirstByUserIdAndStatusOrderByStartAtDesc(userId, SubscriptionStatus.ACTIVE)
            .orElseGet(() -> getOrCreateFreeSubscription(userId));
        return toDto(subscription);
    }

    @Transactional
    public SubscriptionDto payForPlan(UUID userId, String planCode) {
        log.info("Processing payment for user: {}, plan: {}", userId, planCode);
        
        Plan plan = planService.getPlanEntityByCode(planCode);
        User user = authService.getUserById(userId);
        
        // Cancel all existing active subscriptions (handle duplicates)
        List<Subscription> activeSubscriptions = subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
        activeSubscriptions.forEach(sub -> {
            sub.setStatus(SubscriptionStatus.CANCELED);
            sub.setEndAt(LocalDateTime.now());
            subscriptionRepository.save(sub);
        });

        // Create new active subscription
        Subscription subscription = Subscription.builder()
            .user(user)
            .plan(plan)
            .status(SubscriptionStatus.ACTIVE)
            .startAt(LocalDateTime.now())
            .build();

        subscription = subscriptionRepository.save(subscription);
        log.info("Subscription activated: user={}, plan={}", userId, planCode);
        return toDto(subscription);
    }

    @Transactional
    public void cancelSubscription(UUID userId) {
        log.info("Canceling subscription for user: {}", userId);
        List<Subscription> activeSubscriptions = subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
        activeSubscriptions.forEach(sub -> {
            sub.setStatus(SubscriptionStatus.CANCELED);
            sub.setEndAt(LocalDateTime.now());
            subscriptionRepository.save(sub);
            log.info("Subscription canceled: {}", sub.getId());
        });
    }

    private Subscription getOrCreateFreeSubscription(UUID userId) {
        Plan freePlan = planService.getPlanEntityByCode("FREE");
        User user = authService.getUserById(userId);
        
        Subscription subscription = Subscription.builder()
            .user(user)
            .plan(freePlan)
            .status(SubscriptionStatus.ACTIVE)
            .startAt(LocalDateTime.now())
            .build();
        return subscriptionRepository.save(subscription);
    }

    private SubscriptionDto toDto(Subscription subscription) {
        return SubscriptionDto.builder()
            .id(subscription.getId())
            .planId(subscription.getPlan().getId())
            .planCode(subscription.getPlan().getCode())
            .planName(subscription.getPlan().getName())
            .status(subscription.getStatus().name())
            .startAt(subscription.getStartAt())
            .endAt(subscription.getEndAt())
            .build();
    }
}
