package com.shiptrack.notification_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

/**
 * One record per consumed shipment event. Doubles as the data source for
 * the Reporting Module - trackingNumber/status/origin/destination are
 * pulled from the event payload snapshot rather than re-querying
 * shipment-service, keeping notification-service fully decoupled from it.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private String shipmentId;

    private String trackingNumber;

    private ShipmentEventType eventType;

    private NotificationStatus notificationStatus;

    /** Snapshot of relevant shipment fields at the time the event was consumed. */
    private Map<String, Object> payload;

    /** When the underlying shipment event actually happened (from the Kafka message). */
    private Instant eventTimestamp;

    /** When this service finished processing (or failed to process) the event. */
    private Instant processedAt;

    /** Populated only when notificationStatus == FAILED, for retry/debugging. */
    private String failureReason;

    /** How many times processing has been retried for this record. */
    @Builder.Default
    private int retryCount = 0;
}
