package com.shiptrack.notification_service.service;

import com.shiptrack.notification_service.dto.response.DeliveredShipmentResponse;
import com.shiptrack.notification_service.dto.response.EventProcessingReportResponse;
import com.shiptrack.notification_service.dto.response.ShipmentStatusSummaryResponse;
import com.shiptrack.notification_service.entity.Notification;
import com.shiptrack.notification_service.entity.NotificationStatus;
import com.shiptrack.notification_service.entity.ShipmentEventType;
import com.shiptrack.notification_service.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(notificationRepository);
    }

    private Notification buildNotification(String shipmentId, ShipmentEventType type,
                                           NotificationStatus status, String shipmentStatus, Instant ts) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", shipmentStatus);
        payload.put("customerId", "johndoe");
        payload.put("origin", "Mumbai");
        payload.put("destination", "Delhi");

        return Notification.builder()
                .id("n-" + shipmentId + "-" + ts.toEpochMilli())
                .shipmentId(shipmentId)
                .trackingNumber("TRK-" + shipmentId)
                .eventType(type)
                .notificationStatus(status)
                .payload(payload)
                .eventTimestamp(ts)
                .processedAt(ts)
                .retryCount(0)
                .build();
    }

    @Test
    void getShipmentStatusSummary_shouldCountOnlyLatestEventPerShipment() {
        Instant t1 = Instant.parse("2026-01-01T00:00:00Z");
        Instant t2 = Instant.parse("2026-01-02T00:00:00Z");

        // s1: created then delivered - only DELIVERED should count
        Notification s1Created = buildNotification("s1", ShipmentEventType.SHIPMENT_CREATED,
                NotificationStatus.PROCESSED, "CREATED", t1);
        Notification s1Delivered = buildNotification("s1", ShipmentEventType.SHIPMENT_DELIVERED,
                NotificationStatus.PROCESSED, "DELIVERED", t2);

        // s2: only created
        Notification s2Created = buildNotification("s2", ShipmentEventType.SHIPMENT_CREATED,
                NotificationStatus.PROCESSED, "CREATED", t1);

        when(notificationRepository.findAll()).thenReturn(List.of(s1Created, s1Delivered, s2Created));

        ShipmentStatusSummaryResponse summary = reportService.getShipmentStatusSummary();

        assertThat(summary.getTotalShipments()).isEqualTo(2);
        assertThat(summary.getCountsByStatus().get("DELIVERED")).isEqualTo(1L);
        assertThat(summary.getCountsByStatus().get("CREATED")).isEqualTo(1L);
    }

    @Test
    void getDeliveredShipments_shouldReturnOnlyDeliveredEvents_sortedNewestFirst() {
        Instant t1 = Instant.parse("2026-01-01T00:00:00Z");
        Instant t2 = Instant.parse("2026-01-02T00:00:00Z");

        Notification older = buildNotification("s1", ShipmentEventType.SHIPMENT_DELIVERED,
                NotificationStatus.PROCESSED, "DELIVERED", t1);
        Notification newer = buildNotification("s2", ShipmentEventType.SHIPMENT_DELIVERED,
                NotificationStatus.PROCESSED, "DELIVERED", t2);

        when(notificationRepository.findByEventType(ShipmentEventType.SHIPMENT_DELIVERED))
                .thenReturn(List.of(older, newer));

        List<DeliveredShipmentResponse> results = reportService.getDeliveredShipments();

        assertThat(results).hasSize(2);
        // newest first
        assertThat(results.get(0).getShipmentId()).isEqualTo("s2");
        assertThat(results.get(1).getShipmentId()).isEqualTo("s1");
        assertThat(results.get(0).getCustomerId()).isEqualTo("johndoe");
    }

    @Test
    void getDelayedShipments_shouldReturnOnlyShipmentsWhoseLatestStatusIsDelayed() {
        Instant t1 = Instant.parse("2026-01-01T00:00:00Z");
        Instant t2 = Instant.parse("2026-01-02T00:00:00Z");

        // s1: created then delayed -> should appear
        Notification s1Created = buildNotification("s1", ShipmentEventType.SHIPMENT_CREATED,
                NotificationStatus.PROCESSED, "CREATED", t1);
        Notification s1Delayed = buildNotification("s1", ShipmentEventType.SHIPMENT_STATUS_UPDATED,
                NotificationStatus.PROCESSED, "DELAYED", t2);

        // s2: only delivered -> should NOT appear
        Notification s2Delivered = buildNotification("s2", ShipmentEventType.SHIPMENT_DELIVERED,
                NotificationStatus.PROCESSED, "DELIVERED", t1);

        when(notificationRepository.findAll()).thenReturn(List.of(s1Created, s1Delayed, s2Delivered));

        List<DeliveredShipmentResponse> results = reportService.getDelayedShipments();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getShipmentId()).isEqualTo("s1");
    }

    @Test
    void getDailyEventReport_shouldCountProcessedAndFailedSeparately() {
        LocalDate targetDate = LocalDate.of(2026, 1, 15);
        Instant withinDay1 = targetDate.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(3600);
        Instant withinDay2 = targetDate.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(7200);

        Notification processed = buildNotification("s1", ShipmentEventType.SHIPMENT_CREATED,
                NotificationStatus.PROCESSED, "CREATED", withinDay1);
        Notification failed = buildNotification("s2", ShipmentEventType.SHIPMENT_STATUS_UPDATED,
                NotificationStatus.FAILED, "IN_TRANSIT", withinDay2);

        when(notificationRepository.findByEventTimestampBetween(any(), any()))
                .thenReturn(List.of(processed, failed));

        EventProcessingReportResponse report = reportService.getDailyEventReport(targetDate);

        assertThat(report.getDate()).isEqualTo(targetDate);
        assertThat(report.getTotalEvents()).isEqualTo(2);
        assertThat(report.getProcessedCount()).isEqualTo(1);
        assertThat(report.getFailedCount()).isEqualTo(1);
        assertThat(report.getCountsByEventType())
                .containsEntry("SHIPMENT_CREATED", 1L)
                .containsEntry("SHIPMENT_STATUS_UPDATED", 1L);
    }

    @Test
    void getDailyEventReport_shouldDefaultToToday_whenDateIsNull() {
        when(notificationRepository.findByEventTimestampBetween(any(), any()))
                .thenReturn(List.of());

        EventProcessingReportResponse report = reportService.getDailyEventReport(null);

        assertThat(report.getDate()).isEqualTo(LocalDate.now(ZoneOffset.UTC));
        assertThat(report.getTotalEvents()).isEqualTo(0);
    }
}