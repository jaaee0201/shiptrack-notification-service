package com.shiptrack.notification_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Counts of shipments currently in each status, derived from the latest
 * known event per shipment (this service's local read-model built from
 * consumed Kafka events, not a live query against shipment-service).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentStatusSummaryResponse {
    private Map<String, Long> countsByStatus;
    private long totalShipments;
}
