package com.orionticket.notifications.infrastructure.adapters.in.rest.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.orionticket.notifications.application.port.in.model.NotificationPage;
import com.orionticket.notifications.domain.model.Notification;
import com.orionticket.notifications.infrastructure.adapters.in.rest.dto.NotificationDetailResponse;
import com.orionticket.notifications.infrastructure.adapters.in.rest.dto.NotificationListResponse;
import com.orionticket.notifications.infrastructure.adapters.in.rest.dto.NotificationRetryResponse;
import com.orionticket.notifications.infrastructure.adapters.in.rest.dto.NotificationSummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class NotificationRestMapper {

    private final ObjectMapper objectMapper;

    public NotificationRestMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public NotificationListResponse toListResponse(NotificationPage page) {
        return new NotificationListResponse(
                page.notifications().stream()
                        .map(this::toSummaryResponse)
                        .toList(),
                page.page(),
                page.totalPages()
        );
    }

    public NotificationDetailResponse toDetailResponse(Notification notification) {
        return new NotificationDetailResponse(
                notification.notificationId(),
                notification.recipientId(),
                notification.channel().name(),
                notification.templateId(),
                toJsonNode(notification.payload()),
                notification.status().name(),
                notification.retryCount(),
                notification.triggeredBy(),
                notification.createdAt()
        );
    }

    public NotificationRetryResponse toRetryResponse(Notification notification) {
        return new NotificationRetryResponse(
                notification.notificationId(),
                notification.recipientId(),
                notification.channel().name(),
                notification.status().name(),
                notification.retryCount()
        );
    }

    private NotificationSummaryResponse toSummaryResponse(Notification notification) {
        return new NotificationSummaryResponse(
                notification.notificationId(),
                notification.recipientId(),
                notification.channel().name(),
                notification.templateId(),
                notification.status().name(),
                notification.retryCount(),
                notification.triggeredBy(),
                notification.createdAt()
        );
    }

    private JsonNode toJsonNode(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (JsonProcessingException ex) {
            return TextNode.valueOf(payload);
        }
    }
}
