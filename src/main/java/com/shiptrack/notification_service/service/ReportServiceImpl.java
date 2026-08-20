package com.shiptrack.notification_service.service;


import com.shiptrack.notification_service.dto.response.DeliveredShipmentResponse;
import com.shiptrack.notification_service.dto.response.EventProcessingReportResponse;
import com.shiptrack.notification_service.dto.response.NotificationResponse;
import com.shiptrack.notification_service.dto.response.ShipmentStatusSummaryResponse;
import com.shiptrack.notification_service.entity.Notification;

import com.shiptrack.notification_service.entity.NotificationStatus;
import com.shiptrack.notification_service.entity.ShipmentEventType;
import com.shiptrack.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final NotificationService notificationService;


    /**
     * Shipment Status Summary: counts shipments by their MOST RECENT known
     * status.
     */
    @Override
    public ShipmentStatusSummaryResponse getShipmentStatusSummary() {
        List<Notification> all = notificationService.getAllNotificationWtMapping();


        /*
        this will give record of lastupdatedstatus of shipment
         */
        Map<String, Notification> latestPerShipment = all.stream()
                .collect(Collectors.toMap(
                        Notification::getShipmentId,
                        Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparing(Notification::getEventTimestamp))
                ));
     /*
        group by status
      */
                Map<String, Long> countsByStatus = latestPerShipment.values().stream()
            .map(n -> n.getPayload() != null ? String.valueOf(n.getPayload().get("status")) : "UNKNOWN")
                        .collect(Collectors.groupingBy(
                                Function.identity(),
                                LinkedHashMap::new,
                                Collectors.counting()
                        ));

        return ShipmentStatusSummaryResponse.builder()
            .countsByStatus(countsByStatus)
            .totalShipments(latestPerShipment.size())
            .build();
    }


    /** Delivered Shipments Report: every shipment whose latest event was a delivery. */
    @Override
    public List<DeliveredShipmentResponse> getDeliveredShipments() {
        return notificationService.findByEventType(ShipmentEventType.SHIPMENT_DELIVERED).stream()
            .sorted(Comparator.comparing(Notification::getEventTimestamp).reversed())
            .map(n -> DeliveredShipmentResponse.builder()
                .shipmentId(n.getShipmentId())
                .trackingNumber(n.getTrackingNumber())
                .customerId(n.getPayload() != null ? String.valueOf(n.getPayload().get("customerId")) : null)
                .origin(n.getPayload() != null ? String.valueOf(n.getPayload().get("origin")) : null)
                .destination(n.getPayload() != null ? String.valueOf(n.getPayload().get("destination")) : null)
                .deliveredAt(n.getEventTimestamp())
                .build())
            .toList();
    }

    /**
     * Delayed Shipment Report: shipments whose latest known status is
     * DELAYED
     */
    @Override
    public List<DeliveredShipmentResponse> getDelayedShipments() {
        List<Notification> all = notificationService.getAllNotificationWtMapping();

        Map<String, Notification> latestPerShipment = all.stream()
                .collect(Collectors.toMap(
                        Notification::getShipmentId,
                        Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparing(Notification::getEventTimestamp))
                ));

        return latestPerShipment.values().stream()
            .filter(n -> n.getPayload() != null && "DELAYED".equals(String.valueOf(n.getPayload().get("status"))))
            .map(n -> DeliveredShipmentResponse.builder()
                .shipmentId(n.getShipmentId())
                .trackingNumber(n.getTrackingNumber())
                .customerId(String.valueOf(n.getPayload().get("customerId")))
                .origin(String.valueOf(n.getPayload().get("origin")))
                .destination(String.valueOf(n.getPayload().get("destination")))
                .deliveredAt(n.getEventTimestamp())
                .build())
            .toList();
    }

    /** Daily Event Processing Report: today's events, broken down by type, processed vs failed. */
    public EventProcessingReportResponse getDailyEventReport(LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now(ZoneOffset.UTC);
        Instant startOfDay = targetDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endOfDay = targetDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<Notification> dayEvents = notificationService.findByEventTimestampBetween(startOfDay, endOfDay);

        long processed = dayEvents.stream()
            .filter(n -> n.getNotificationStatus() == NotificationStatus.PROCESSED)
            .count();
        long failed = dayEvents.stream()
            .filter(n -> n.getNotificationStatus() == NotificationStatus.FAILED)
            .count();

        Map<String, Long> countsByType = dayEvents.stream()
            .collect(Collectors.groupingBy(
                n -> n.getEventType().name(), LinkedHashMap::new, Collectors.counting()));

        return EventProcessingReportResponse.builder()
            .date(targetDate)
            .totalEvents(dayEvents.size())
            .processedCount(processed)
            .failedCount(failed)
            .countsByEventType(countsByType)
            .build();
    }
}
