package com.orionticket.notifications.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitMQConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(RabbitMQConfig.class);

    @Test
    void declaresNotificationEventsExchangeQueueAndTicketIssuedBinding() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(TopicExchange.class);
            assertThat(context).hasSingleBean(Queue.class);
            assertThat(context).hasSingleBean(Binding.class);

            TopicExchange exchange = context.getBean(TopicExchange.class);
            Queue queue = context.getBean(Queue.class);
            Binding binding = context.getBean(Binding.class);

            assertThat(exchange.getName()).isEqualTo("notification-events");
            assertThat(queue.getName()).isEqualTo("notification-events");
            assertThat(binding.getExchange()).isEqualTo("notification-events");
            assertThat(binding.getRoutingKey()).isEqualTo("ticket.issued");
        });
    }
}
