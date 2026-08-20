package com.shiptrack.notification_service.service;

import com.shiptrack.notification_service.dto.response.NotificationResponse;
import com.shiptrack.notification_service.entity.Notification;
import com.shiptrack.notification_service.entity.NotificationStatus;
import com.shiptrack.notification_service.entity.ShipmentEventType;
import com.shiptrack.notification_service.exception.NotificationNotFoundException;
import com.shiptrack.notification_service.mapper.NotificationMapper;
import com.shiptrack.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements  NotificationService{

    private final NotificationRepository notificationRepository;
    private final NotificationMapper mapper;


    @Override
    public List<NotificationResponse> getAllNotifications() {
        return getAllNotificationWtMapping().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<Notification> getAllNotificationWtMapping(){
        return notificationRepository.findAll();
    }

    @Override
    public NotificationResponse getNotificationById(String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("No notification found with id '" + id + "'"));
        return mapper.toResponse(notification);
    }

    @Override
    public  List<Notification> findByNotificationStatus(NotificationStatus status){
        return notificationRepository.findByNotificationStatus(status);
    }

    @Override
    public  List<Notification> findByEventType(ShipmentEventType eventType){
        return notificationRepository.findByEventType((eventType));
    }
    @Override
    public  List<Notification> findByEventTimestampBetween(Instant start, Instant end){
        return notificationRepository.findByEventTimestampBetween(start,end);
    }
    public List<NotificationResponse> getNotificationsForShipment(String shipmentId) {
        return notificationRepository.findByShipmentId(shipmentId).stream()
                .map(mapper::toResponse)
                .toList();
    }
}
