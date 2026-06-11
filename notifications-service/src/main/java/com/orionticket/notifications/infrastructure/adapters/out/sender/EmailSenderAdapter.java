package com.orionticket.notifications.infrastructure.adapters.out.sender;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orionticket.notifications.domain.model.Notification;
import com.orionticket.notifications.domain.model.NotificationSendResult;
import com.orionticket.notifications.domain.port.out.NotificationSenderPort;
import com.orionticket.notifications.infrastructure.config.ResendProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class EmailSenderAdapter implements NotificationSenderPort {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderAdapter.class);

    private final ResendProperties resendProperties;
    private final TemplateEngine templateEngine;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public EmailSenderAdapter(
            ResendProperties resendProperties,
            TemplateEngine templateEngine,
            ObjectMapper objectMapper,
            RestTemplate restTemplate
    ) {
        this.resendProperties = resendProperties;
        this.templateEngine = templateEngine;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    @Override
    public NotificationSendResult send(Notification notification) {
        log.info("Processing notification dispatch: {}", notification.notificationId());

        // Parse JSON payload
        Map<String, Object> payloadMap;
        try {
            payloadMap = objectMapper.readValue(notification.payload(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("Failed to parse notification payload JSON: {}", notification.payload(), e);
            return NotificationSendResult.failure("Invalid payload JSON: " + e.getMessage());
        }

        // Render template using Thymeleaf
        Context context = new Context();
        payloadMap.forEach(context::setVariable);

        String htmlContent;
        try {
            htmlContent = templateEngine.process(notification.templateId(), context);
        } catch (Exception e) {
            log.error("Failed to render Thymeleaf template: {}", notification.templateId(), e);
            return NotificationSendResult.failure("Template rendering failed: " + e.getMessage());
        }

        // Determine destination email
        String recipientEmail = (String) payloadMap.get("recipientEmail");
        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.error("Recipient email is missing in notification payload");
            return NotificationSendResult.failure("Missing recipientEmail in payload");
        }

        String subject = (String) payloadMap.getOrDefault("subject", "Tu ticket de OrionTicket");

        // Check if email dispatch is enabled
        if (!resendProperties.isEnabled()) {
            log.info("[MOCK EMAIL DISPATCH] To: {}, Subject: {}\nHTML Content:\n{}", recipientEmail, subject, htmlContent);
            return NotificationSendResult.success("mock-message-id-" + notification.notificationId());
        }

        // Build Resend API request
        Map<String, Object> requestBody = new HashMap<>();
        String fromHeader = resendProperties.getFromName() + " <" + resendProperties.getFromEmail() + ">";
        requestBody.put("from", fromHeader);
        requestBody.put("to", Collections.singletonList(recipientEmail));
        requestBody.put("subject", subject);
        requestBody.put("html", htmlContent);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(resendProperties.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    "https://api.resend.com/emails",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String messageId = (String) response.getBody().get("id");
                log.info("Successfully sent email to {} via Resend. Message ID: {}", recipientEmail, messageId);
                return NotificationSendResult.success(messageId != null ? messageId : "unknown-id");
            } else {
                log.error("Failed to send email via Resend, status: {}", response.getStatusCode());
                return NotificationSendResult.failure("Resend API returned status code: " + response.getStatusCode());
            }
        } catch (HttpStatusCodeException e) {
            log.error("Resend API returned error. Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return NotificationSendResult.failure("Resend API error (" + e.getStatusCode() + "): " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("HTTP call to Resend API failed", e);
            return NotificationSendResult.failure("HTTP call failed: " + e.getMessage());
        }
    }
}
