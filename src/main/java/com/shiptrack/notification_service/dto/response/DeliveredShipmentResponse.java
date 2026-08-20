package com.shiptrack.notification_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveredShipmentResponse {
    private String shipmentId;
    private String trackingNumber;
    private String customerId;
    private String origin;
    private String destination;
    private Instant deliveredAt;
}
