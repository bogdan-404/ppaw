package com.ppaw.service;

import com.ppaw.dataaccess.entity.Plan;
import com.ppaw.dataaccess.entity.SavedWork;
import com.ppaw.dataaccess.entity.Subscription;
import com.ppaw.dataaccess.entity.Subscription.SubscriptionStatus;
import com.ppaw.dataaccess.entity.UsageLog;
import com.ppaw.dataaccess.entity.User;
import com.ppaw.dataaccess.repository.SavedWorkRepository;
import com.ppaw.dataaccess.repository.SubscriptionRepository;
import com.ppaw.dataaccess.repository.UsageLogRepository;
import com.ppaw.service.dto.TextRequest;
import com.ppaw.service.dto.TextResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TextService {

    private final SubscriptionRepository subscriptionRepository;
    private final UsageLogRepository usageLogRepository;
    private final SavedWorkRepository savedWorkRepository;
    private final PlanService planService;
    private final AuthService authService;
    private final CacheManager cacheManager;
    
    private final Lorem lorem = LoremIpsum.getInstance();
    private final Random random = new Random();
    
    private User getUserById(UUID userId) {
        return authService.getUserById(userId);
    }

    @Transactional
    public TextResponse summarize(UUID userId, TextRequest request) {
        log.info("Summarize request from user: {}, chars: {}", userId, request.getText().length());
        
        Subscription subscription = getActiveSubscription(userId);
        Plan plan = subscription.getPlan();
        Map<String, String> limits = getPlanLimits(plan);

        // Validate text length
        int maxChars = Integer.parseInt(limits.getOrDefault("max_chars", "2000"));
        if (request.getText().length() > maxChars) {
            log.warn("Request denied: text too long. User: {}, length: {}, max: {}", 
                userId, request.getText().length(), maxChars);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Text exceeds maximum length of " + maxChars + " characters");
        }

        // Validate style
        validateStyle(request.getStyle(), limits);

        // Check limits
        checkLimits(userId, plan, limits);

        // Generate output (simulate AI)
        String output = generateSummary(request.getText(), request.getStyle());
        
        User user = getUserById(userId);
        
        // Log usage
        UsageLog usageLog = UsageLog.builder()
            .user(user)
            .actionType(UsageLog.ActionType.SUMMARIZE)
            .charsIn(request.getText().length())
            .createdAt(LocalDateTime.now())
            .build();
        usageLogRepository.save(usageLog);
        log.info("Usage logged: user={}, type=SUMMARIZE", userId);

        // Save to history
        SavedWork savedWork = SavedWork.builder()
            .user(user)
            .workType(SavedWork.WorkType.SUMMARIZE)
            .inputText(request.getText())
            .outputText(output)
            .style(request.getStyle())
            .createdAt(LocalDateTime.now())
            .build();
        savedWork = savedWorkRepository.save(savedWork);
        
        // Evict cache for this user's history
        if (cacheManager.getCache("history") != null) {
            cacheManager.getCache("history").evictIfPresent(userId);
            log.info("Cache evicted for user history: {}", userId);
        }
        
        log.info("Work saved to history: {}", savedWork.getId());

        return new TextResponse(output, savedWork.getId());
    }

    @Transactional
    public TextResponse rewrite(UUID userId, TextRequest request) {
        log.info("Rewrite request from user: {}, chars: {}", userId, request.getText().length());
        
        Subscription subscription = getActiveSubscription(userId);
        Plan plan = subscription.getPlan();
        Map<String, String> limits = getPlanLimits(plan);

        // Validate text length
        int maxChars = Integer.parseInt(limits.getOrDefault("max_chars", "2000"));
        if (request.getText().length() > maxChars) {
            log.warn("Request denied: text too long. User: {}, length: {}, max: {}", 
                userId, request.getText().length(), maxChars);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Text exceeds maximum length of " + maxChars + " characters");
        }

        // Validate style
        validateStyle(request.getStyle(), limits);

        // Check limits
        checkLimits(userId, plan, limits);

        // Generate output (simulate AI)
        String output = generateRewrite(request.getText(), request.getStyle());
        
        User user = getUserById(userId);
        
        // Log usage
        UsageLog usageLog = UsageLog.builder()
            .user(user)
            .actionType(UsageLog.ActionType.REWRITE)
            .charsIn(request.getText().length())
            .createdAt(LocalDateTime.now())
            .build();
        usageLogRepository.save(usageLog);
        log.info("Usage logged: user={}, type=REWRITE", userId);

        // Save to history
        SavedWork savedWork = SavedWork.builder()
            .user(user)
            .workType(SavedWork.WorkType.REWRITE)
            .inputText(request.getText())
            .outputText(output)
            .style(request.getStyle())
            .createdAt(LocalDateTime.now())
            .build();
        savedWork = savedWorkRepository.save(savedWork);
        
        // Evict cache for this user's history
        if (cacheManager.getCache("history") != null) {
            cacheManager.getCache("history").evictIfPresent(userId);
            log.info("Cache evicted for user history: {}", userId);
        }
        
        log.info("Work saved to history: {}", savedWork.getId());

        return new TextResponse(output, savedWork.getId());
    }

    private Subscription getActiveSubscription(UUID userId) {
        return subscriptionRepository.findFirstByUserIdAndStatusOrderByStartAtDesc(userId, SubscriptionStatus.ACTIVE)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No active subscription"));
    }

    private Map<String, String> getPlanLimits(Plan plan) {
        return planService.getPlanByCode(plan.getCode()).getLimits();
    }

    private void validateStyle(String style, Map<String, String> limits) {
        if (style == null || style.isEmpty()) {
            return;
        }
        String allowedStyles = limits.getOrDefault("allowed_styles", "");
        List<String> allowed = Arrays.asList(allowedStyles.split(","));
        if (!allowed.contains(style)) {
            log.warn("Style not allowed: {}", style);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "Style '" + style + "' is not allowed for your plan");
        }
    }

    private void checkLimits(UUID userId, Plan plan, Map<String, String> limits) {
        String planCode = plan.getCode();
        
        if ("PREMIUM".equals(planCode) && "true".equals(limits.get("unlimited_requests"))) {
            return; // Unlimited
        }

        if ("FREE".equals(planCode)) {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            long dailyCount = usageLogRepository.countByUser_IdAndCreatedAtAfter(userId, startOfDay);
            int dailyLimit = Integer.parseInt(limits.getOrDefault("daily_requests", "3"));
            if (dailyCount >= dailyLimit) {
                log.warn("Request denied: daily limit reached. User: {}, count: {}", userId, dailyCount);
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, 
                    "Daily limit of " + dailyLimit + " requests reached");
            }
        } else if ("USUAL".equals(planCode)) {
            LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
            LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();
            long monthlyCount = usageLogRepository.countByUser_IdAndCreatedAtAfter(userId, startOfMonth);
            int monthlyLimit = Integer.parseInt(limits.getOrDefault("monthly_requests", "200"));
            if (monthlyCount >= monthlyLimit) {
                log.warn("Request denied: monthly limit reached. User: {}, count: {}", userId, monthlyCount);
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, 
                    "Monthly limit of " + monthlyLimit + " requests reached");
            }
        }
    }

    private String generateSummary(String text, String style) {
        // Generate random lorem ipsum summary
        // Use 20-50% of input word count, with minimum 10 and maximum 100 words
        int inputWordCount = text.split("\\s+").length;
        int minWords = Math.max(10, inputWordCount / 5); // 20% of input, min 10
        int maxWords = Math.min(100, inputWordCount / 2); // 50% of input, max 100
        int wordCount = minWords + random.nextInt(Math.max(1, maxWords - minWords + 1));
        return lorem.getWords(wordCount);
    }

    private String generateRewrite(String text, String style) {
        // Generate random lorem ipsum rewrite
        // Use 80-120% of input word count, with minimum 20 words
        int inputWordCount = text.split("\\s+").length;
        int minWords = Math.max(20, (int) (inputWordCount * 0.8)); // 80% of input, min 20
        int maxWords = (int) (inputWordCount * 1.2); // 120% of input
        int wordCount = minWords + random.nextInt(Math.max(1, maxWords - minWords + 1));
        return lorem.getWords(wordCount);
    }
}
