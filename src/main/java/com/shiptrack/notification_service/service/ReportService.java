package com.shiptrack.notification_service.service;

import com.shiptrack.notification_service.dto.response.DeliveredShipmentResponse;
import com.shiptrack.notification_service.dto.response.ShipmentStatusSummaryResponse;

import java.util.List;

public interface ReportService {
    ShipmentStatusSummaryResponse getShipmentStatusSummary();
    List<DeliveredShipmentResponse> getDeliveredShipments();
    List<DeliveredShipmentResponse> getDelayedShipments();
}
