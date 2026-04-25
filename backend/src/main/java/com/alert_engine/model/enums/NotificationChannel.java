package com.alert_engine.model.enums;

/**
 * Supported channels for alert notifications.
 * <p>
 * EMAIL: send an email to the user.
 * IN_APP: show a notification within the application (e.g., dashboard).
 * SMS: send a text message to the user's phone.
 * PUSH_NOTIFICATION: send a push notification to the user's device.
 */
public enum NotificationChannel {
    EMAIL,
    IN_APP,
    SMS,
    PUSH_NOTIFICATION,

}
