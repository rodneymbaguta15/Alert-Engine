package com.alert_engine.model;

import com.alert_engine.model.enums.AlertDirection;
import com.alert_engine.model.enums.DeliveryStatus;
import com.alert_engine.model.enums.NotificationChannel;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A record of an alert that was triggered, including the price that caused the trigger,
 * the delivery status of the notification, and any error messages if delivery failed.
 * <p>
 * This allows us to show users a history of their alerts and troubleshoot any issues with notifications.
 */

@Entity
@Table(name = "alert_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alert_config_id", nullable = false)
    private AlertConfig alertConfig;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 10)
    private String ticker;

    @Column(name = "triggered_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal triggeredPrice;

    @Column(name = "threshold_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal thresholdPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AlertDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 30)
    private DeliveryStatus deliveryStatus;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "triggered_at", nullable = false, insertable = false, updatable = false)
    private Instant triggeredAt;
}