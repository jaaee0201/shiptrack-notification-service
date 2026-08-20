package com.shiptrack.notification_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * Daily Event Processing Report: how many events were processed (and how
 * many failed) on a given day, broken down by event type.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventProcessingReportResponse {
    private LocalDate date;
    private long totalEvents;
    private long processedCount;
    private long failedCount;
    private Map<String, Long> countsByEventType;
}
