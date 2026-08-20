package com.shiptrack.notification_service.dto.response;


import com.shiptrack.notification_service.entity.NotificationStatus;
import com.shiptrack.notification_service.entity.ShipmentEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private String id;
    private String shipmentId;
    private String trackingNumber;
    private ShipmentEventType eventType;
    private NotificationStatus notificationStatus;
    private Map<String, Object> payload;
    private Instant eventTimestamp;
    private Instant processedAt;
    private String failureReason;
    private int retryCount;
}
