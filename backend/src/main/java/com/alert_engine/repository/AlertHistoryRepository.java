package com.alert_engine.repository;

import com.alert_engine.model.AlertHistory;
import com.alert_engine.model.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertHistoryRepository extends JpaRepository<AlertHistory, Long> {

    /**
     * Paginated, most-recent-first history for a user. Backs the /history page.
     */
    Page<AlertHistory> findByUserIdOrderByTriggeredAtDesc(Long userId, Pageable pageable);

    /**
     * Core cooldown query. Returns the most recent SENT history entry for a given
     * alert config. The evaluator compares `triggered_at` against now() - cooldownSeconds.
     *
     * Note: we filter on SENT specifically — a SUPPRESSED_COOLDOWN row should NOT
     * reset the cooldown window, and a FAILED delivery shouldn't either (the user
     * never actually got the alert).
     */
    @Query("""
        SELECT h FROM AlertHistory h
        WHERE h.alertConfig.id = :configId
          AND h.deliveryStatus = :status
        ORDER BY h.triggeredAt DESC
        LIMIT 1
    """)
    Optional<AlertHistory> findLatestByConfigAndStatus(
            @Param("configId") Long configId,
            @Param("status") DeliveryStatus status);

    /**
     * All history for a single config — lets a trader drill into "why did this alert fire?"
     */
    List<AlertHistory> findByAlertConfigIdOrderByTriggeredAtDesc(Long alertConfigId);

    /**
     * Filtered feed for the history page: specific ticker within a date range.
     */
    Page<AlertHistory> findByUserIdAndTickerAndTriggeredAtBetween(
            Long userId,
            String ticker,
            Instant from,
            Instant to,
            Pageable pageable);
}