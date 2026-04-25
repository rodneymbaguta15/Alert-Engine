package com.alert_engine.model;

import com.alert_engine.model.converter.NotificationChannelSetConverter;
import com.alert_engine.model.enums.AlertDirection;
import com.alert_engine.model.enums.NotificationChannel;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

/**
 * A trader-defined price alert rule: "notify me when {ticker} goes {direction}
 * {thresholdPrice}, respecting a {cooldownSeconds} cooldown between alerts."
 * <p>
 * `isArmed` supports once-per-crossing semantics: the evaluator fires only when
 * the price crosses the threshold , and re-arms once the price returns to the opposite side.
 */
@Entity
@Table(name = "alert_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ManyToOne over a raw userId field: lets us navigate config.getUser() in services
    // without an extra lookup. LAZY because we usually don't need the full user.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 10)
    private String ticker;

    @Column(name = "threshold_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal thresholdPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AlertDirection direction;

    @Column(name = "cooldown_seconds", nullable = false)
    @Builder.Default
    private Integer cooldownSeconds = 900;   // 15 min default, matches DB default

    @Convert(converter = NotificationChannelSetConverter.class)
    @Column(nullable = false, length = 100)
    @Builder.Default
    private Set<NotificationChannel> channels = EnumSet.of(NotificationChannel.IN_APP);

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "is_armed", nullable = false)
    @Builder.Default
    private Boolean isArmed = true;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;
}