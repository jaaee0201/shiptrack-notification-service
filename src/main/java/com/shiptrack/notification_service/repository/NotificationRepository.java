package com.shiptrack.notification_service.repository;


import com.shiptrack.notification_service.entity.Notification;
import com.shiptrack.notification_service.entity.NotificationStatus;
import com.shiptrack.notification_service.entity.ShipmentEventType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByShipmentId(String shipmentId);

    List<Notification> findByNotificationStatus(NotificationStatus status);

    List<Notification> findByEventType(ShipmentEventType eventType);

    List<Notification> findByEventTimestampBetween(Instant start, Instant end);

}
