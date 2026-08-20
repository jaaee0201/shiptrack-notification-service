package com.shiptrack.notification_service.entity;

public enum NotificationStatus {
    /** Consumed from Kafka and processed successfully. */
    PROCESSED,
    /** Consumption/processing failed - stored for retry, per business rule. */
    FAILED
}
