package com.shiptrack.notification_service.consumer;

import com.shiptrack.notification_service.entity.ShipmentEventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentEventPayload {
    private String shipmentId;
    private String trackingNumber;
    private ShipmentEventType eventType;
    private Instant eventTimestamp;
    private Map<String, Object> payload;
}
