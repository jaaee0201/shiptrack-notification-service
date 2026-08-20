package com.shiptrack.notification_service.consumer;


import com.shiptrack.notification_service.entity.Notification;

import com.shiptrack.notification_service.entity.NotificationStatus;
import com.shiptrack.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentEventConsumer {

    private final NotificationRepository notificationRepository;


    @Async
    @KafkaListener(topics = "${kafka.topics.shipment-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void handle(ShipmentEventPayload event) {
        try {
            Notification notification = Notification.builder()
                .shipmentId(event.getShipmentId())
                .trackingNumber(event.getTrackingNumber())
                    .eventType(event.getEventType())
                .notificationStatus(NotificationStatus.PROCESSED)
                .payload(event.getPayload())
                .eventTimestamp(event.getEventTimestamp())
                .processedAt(Instant.now())
                .retryCount(0)
                .build();

            notificationRepository.save(notification);
            log.info("Notification created for shipment {} ({})", event.getShipmentId(), event.getEventType());

        } catch (Exception e) {
            // Business rule: failed notification events must be stored for
            // retry processing - so even on failure we persist a record
            // (status FAILED) rather than silently dropping the event.
            log.error("Failed to process shipment event for shipment {}: {}",
                event.getShipmentId(), e.getMessage(), e);

            Notification failed = Notification.builder()
                .shipmentId(event.getShipmentId())
                .trackingNumber(event.getTrackingNumber())
                .eventType(event.getEventType())
                .notificationStatus(NotificationStatus.FAILED)
                .payload(event.getPayload())
                .eventTimestamp(event.getEventTimestamp())
                .processedAt(Instant.now())
                .failureReason(e.getMessage())
                .retryCount(0)
                .build();

            notificationRepository.save(failed);
        }
    }
}
