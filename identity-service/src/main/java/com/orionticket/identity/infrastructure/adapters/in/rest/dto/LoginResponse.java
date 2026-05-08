package com.orionticket.identity.infrastructure.adapters.in.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class LoginResponse {
    private String accessToken;
    private UUID userId;
    private UUID roleId;
}
