package com.ppaw.dataaccess.repository;

import com.ppaw.dataaccess.entity.Subscription;
import com.ppaw.dataaccess.entity.Subscription.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findFirstByUserIdAndStatusOrderByStartAtDesc(UUID userId, SubscriptionStatus status);
    
    List<Subscription> findByUserId(UUID userId);
    
    List<Subscription> findByUserIdOrderByStartAtDesc(UUID userId);
    
    List<Subscription> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);
}
