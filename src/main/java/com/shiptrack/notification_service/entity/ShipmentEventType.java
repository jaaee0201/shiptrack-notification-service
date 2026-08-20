package com.shiptrack.notification_service.entity;

/** Mirrors ShipmentEventType in shipment-service - the two agree only on the
 * JSON contract published to Kafka, not a shared Java type. */
public enum ShipmentEventType {
    SHIPMENT_CREATED,
    SHIPMENT_STATUS_UPDATED,
    SHIPMENT_DELIVERED,
    SHIPMENT_CANCELLED
}
