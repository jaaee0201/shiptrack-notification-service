package com.shiptrack.notification_service.service;

import com.shiptrack.notification_service.dto.response.NotificationResponse;
import com.shiptrack.notification_service.entity.Notification;
import com.shiptrack.notification_service.entity.NotificationStatus;
import com.shiptrack.notification_service.entity.ShipmentEventType;
import com.shiptrack.notification_service.exception.NotificationNotFoundException;
import com.shiptrack.notification_service.mapper.NotificationMapper;
import com.shiptrack.notification_service.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationMapper mapper;
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        mapper = new NotificationMapper();
        notificationService = new NotificationServiceImpl(notificationRepository, mapper);
    }

    private Notification buildNotification(String id, String shipmentId, ShipmentEventType type,
                                           NotificationStatus status, Instant ts) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", "CREATED");
        payload.put("customerId", "johndoe");
        payload.put("origin", "Mumbai");
        payload.put("destination", "Delhi");

        return Notification.builder()
                .id(id)
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
    void getAllNotifications_shouldReturnMappedList() {
        Notification n1 = buildNotification("n1", "s1", ShipmentEventType.SHIPMENT_CREATED,
                NotificationStatus.PROCESSED, Instant.now());
        Notification n2 = buildNotification("n2", "s2", ShipmentEventType.SHIPMENT_DELIVERED,
                NotificationStatus.PROCESSED, Instant.now());

        when(notificationRepository.findAll()).thenReturn(List.of(n1, n2));

        List<NotificationResponse> results = notificationService.getAllNotifications();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getShipmentId()).isEqualTo("s1");
        assertThat(results.get(1).getShipmentId()).isEqualTo("s2");
    }

    @Test
    void getNotificationById_shouldThrow_whenNotFound() {
        when(notificationRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getNotificationById("missing"))
                .isInstanceOf(NotificationNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void getNotificationById_shouldReturnMappedResponse_whenFound() {
        Notification n = buildNotification("n1", "s1", ShipmentEventType.SHIPMENT_CREATED,
                NotificationStatus.PROCESSED, Instant.now());
        when(notificationRepository.findById("n1")).thenReturn(Optional.of(n));

        NotificationResponse response = notificationService.getNotificationById("n1");

        assertThat(response.getShipmentId()).isEqualTo("s1");
        assertThat(response.getEventType()).isEqualTo(ShipmentEventType.SHIPMENT_CREATED);
        assertThat(response.getNotificationStatus()).isEqualTo(NotificationStatus.PROCESSED);
    }

    @Test
    void getNotificationsForShipment_shouldReturnOnlyMatchingShipment() {
        Notification n1 = buildNotification("n1", "s1", ShipmentEventType.SHIPMENT_CREATED,
                NotificationStatus.PROCESSED, Instant.now());

        when(notificationRepository.findByShipmentId("s1")).thenReturn(List.of(n1));

        List<NotificationResponse> results = notificationService.getAllNotifications("s1");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getShipmentId()).isEqualTo("s1");
    }
}