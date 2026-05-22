package com.orionticket.seating.shared.infrastructure.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("orionticketdb")
                .withUsername("app_user")
                .withPassword("AppSecret789");
    }

    @Bean
    @ServiceConnection
    public RabbitMQContainer rabbitContainer() {
        return new RabbitMQContainer("rabbitmq:3-management-alpine");
    }
}
