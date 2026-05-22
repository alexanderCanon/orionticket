package com.orionticket.notifications.infrastructure.adapters.in.rest;

import com.orionticket.notifications.application.port.in.NotificationQueryUseCase;
import com.orionticket.notifications.application.port.in.RetryNotificationUseCase;
import com.orionticket.notifications.domain.exception.NotificationNotFoundException;
import com.orionticket.notifications.domain.model.Notification;
import com.orionticket.notifications.domain.model.NotificationChannel;
import com.orionticket.notifications.domain.model.NotificationStatus;
import com.orionticket.notifications.infrastructure.adapters.in.rest.dto.NotificationDetailResponse;
import com.orionticket.notifications.infrastructure.adapters.in.rest.dto.NotificationListResponse;
import com.orionticket.notifications.infrastructure.adapters.in.rest.dto.NotificationRetryResponse;
import com.orionticket.notifications.infrastructure.adapters.in.rest.dto.RetryFailedResponse;
import com.orionticket.notifications.infrastructure.adapters.in.rest.mapper.NotificationRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/notifications")
@Tag(name = "Notifications", description = "Notification delivery log and operational retry endpoints")
public class NotificationController {

    private final NotificationQueryUseCase notificationQueryUseCase;
    private final RetryNotificationUseCase retryNotificationUseCase;
    private final NotificationRestMapper notificationRestMapper;

    public NotificationController(
            NotificationQueryUseCase notificationQueryUseCase,
            RetryNotificationUseCase retryNotificationUseCase,
            NotificationRestMapper notificationRestMapper
    ) {
        this.notificationQueryUseCase = notificationQueryUseCase;
        this.retryNotificationUseCase = retryNotificationUseCase;
        this.notificationRestMapper = notificationRestMapper;
    }

    @Operation(summary = "List notifications", description = "Returns delivery records for operational support and audit visibility.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notifications returned"),
            @ApiResponse(responseCode = "403", description = "Caller is not allowed"),
            @ApiResponse(responseCode = "422", description = "Invalid filters or pagination")
    })
    @GetMapping
    @PreAuthorize("hasRole('SUPPORT') or hasRole('PLATFORM_OPERATOR') or hasRole('SUPER_ADMIN')")
    public NotificationListResponse listNotifications(
            @RequestParam(required = false) UUID recipientId,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) NotificationChannel channel,
            @RequestParam(required = false) String triggeredBy,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return notificationRestMapper.toListResponse(
                notificationQueryUseCase.findNotifications(recipientId, status, channel, triggeredBy, page, size)
        );
    }

    @Operation(summary = "Get notification", description = "Returns a single notification delivery record including payload.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification found"),
            @ApiResponse(responseCode = "403", description = "Caller is not allowed"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @GetMapping("/{notificationId}")
    @PreAuthorize("hasRole('SUPPORT') or hasRole('PLATFORM_OPERATOR') or hasRole('SUPER_ADMIN')")
    public NotificationDetailResponse getNotification(@PathVariable UUID notificationId) {
        return notificationRestMapper.toDetailResponse(
                notificationQueryUseCase.findNotification(notificationId)
                        .orElseThrow(() -> new NotificationNotFoundException(notificationId))
        );
    }

    @Operation(summary = "Retry notification", description = "Retries delivery for a single failed or pending notification.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification retried"),
            @ApiResponse(responseCode = "403", description = "Caller is not allowed"),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "409", description = "Notification cannot be retried")
    })
    @PostMapping("/{notificationId}/retry")
    @PreAuthorize("hasRole('SUPPORT') or hasRole('SUPER_ADMIN')")
    public NotificationRetryResponse retryNotification(@PathVariable UUID notificationId) {
        return notificationRestMapper.toRetryResponse(
                retryNotificationUseCase.retryNotification(notificationId)
        );
    }

    @Operation(summary = "Retry failed notifications", description = "Retries all currently failed or pending notifications.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notifications processed"),
            @ApiResponse(responseCode = "403", description = "Caller is not allowed")
    })
    @PostMapping("/retry-failed")
    @PreAuthorize("hasRole('SUPPORT') or hasRole('PLATFORM_OPERATOR') or hasRole('SUPER_ADMIN')")
    public RetryFailedResponse retryFailedNotifications() {
        List<Notification> notifications = retryNotificationUseCase.retryFailedNotifications();
        int delivered = countByStatus(notifications, NotificationStatus.DELIVERED);
        int failed = countByStatus(notifications, NotificationStatus.FAILED);

        return new RetryFailedResponse(notifications.size(), delivered, failed);
    }

    private int countByStatus(List<Notification> notifications, NotificationStatus status) {
        return (int) notifications.stream()
                .filter(notification -> notification.status() == status)
                .count();
    }
}
