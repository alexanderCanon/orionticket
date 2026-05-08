package com.orionticket.acesscontrol.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class AppConfig {

    @Value("${app.validation.offline-sync-max-records:1000}")
    private int offlineSyncMaxRecords;

    public int getOfflineSyncMaxRecords() {
        return offlineSyncMaxRecords;
    }
}