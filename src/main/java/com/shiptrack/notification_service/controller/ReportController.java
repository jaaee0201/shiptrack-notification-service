package com.shiptrack.notification_service.controller;


import com.shiptrack.notification_service.dto.response.DeliveredShipmentResponse;
import com.shiptrack.notification_service.dto.response.EventProcessingReportResponse;
import com.shiptrack.notification_service.dto.response.ShipmentStatusSummaryResponse;
import com.shiptrack.notification_service.service.ReportServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Reports", description = "Shipment and event processing reports")
@RestController
@RequestMapping("/reports")
@PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
@RequiredArgsConstructor
public class ReportController {

    private final ReportServiceImpl reportService;



    @Operation(summary = "Shipment Status Summary - counts of shipments by current status")
    @GetMapping("/shipments/status")
    public ResponseEntity<ShipmentStatusSummaryResponse> getShipmentStatusSummary() {
        return ResponseEntity.ok(reportService.getShipmentStatusSummary());
    }

    @Operation(summary = "Delivered Shipments Report")
    @GetMapping("/deliveries")
    public ResponseEntity<List<DeliveredShipmentResponse>> getDeliveries() {
        return ResponseEntity.ok(reportService.getDeliveredShipments());
    }

    @Operation(summary = "Delayed Shipment Report")
    @GetMapping("/delayed")
    public ResponseEntity<List<DeliveredShipmentResponse>> getDelayed() {
        return ResponseEntity.ok(reportService.getDelayedShipments());
    }

    @Operation(summary = "Daily Event Processing Report (defaults to today, UTC)")
    @GetMapping("/events")
    public ResponseEntity<EventProcessingReportResponse> getDailyEventReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reportService.getDailyEventReport(date));
    }
}
