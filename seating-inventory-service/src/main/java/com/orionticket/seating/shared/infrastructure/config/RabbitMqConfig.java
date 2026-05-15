package com.orionticket.seating.shared.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RabbitMqConfig {

    // Exchange compartido por todos los microservicios OrionTicket.
    // POR QUÉ Topic Exchange y no Direct: Topic soporta routing keys con wildcards.
    // "reservation.*" matchea "reservation.created" Y "reservation.expired" al mismo tiempo.
    // Direct solo rutea por key exacta; Topic es necesario para que Orders consuma
    // "reservation.*" sin necesitar un binding por cada subtipo de evento.
    public static final String EXCHANGE_NAME = "orionticket.events.exchange";

    // Routing keys que PUBLICA este servicio
    public static final String RESERVATION_CREATED_KEY    = "reservation.created";
    public static final String RESERVATION_EXPIRED_KEY    = "reservation.expired";
    public static final String RESERVATION_RELEASED_KEY   = "reservation.released";
    public static final String BATCH_CREATED_KEY          = "batch.created";
    public static final String BATCH_ACTIVATED_KEY        = "batch.activated";
    public static final String BATCH_EXHAUSTED_KEY        = "batch.exhausted";
    public static final String SEATING_MAP_CONFIGURED_KEY = "seating.map.configured";

    // Routing key que CONSUME este servicio (publicada por Payments Service)
    public static final String PAYMENT_FAILED_KEY = "payment.failed";

    // Dead Letter Exchange: destino para mensajes que agotaron sus reintentos.
    // En lugar de descartarlos para siempre, RabbitMQ los redirige aquí.
    // Un operador puede inspeccionarlos en la UI (localhost:15672) y re-encolarlos
    // manualmente cuando se resuelva el problema subyacente.
    public static final String DLX_EXCHANGE_NAME = "orionticket.seating.dlx";
    public static final String DLQ_NAME          = "orionticket.seating.dlq";

    public static final String PAYMENT_FAILED_QUEUE = "seating.payment.failed.queue";

    // TopicExchange: durable=true → sobrevive reinicios de RabbitMQ.
    // autoDelete=false → no se elimina cuando no hay consumers conectados.
    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    // DirectExchange para la DLX: los mensajes muertos tienen routing key fija,
    // así que Direct es suficiente aquí (no necesitamos wildcards).
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE_NAME, true, false);
    }

    // Dead Letter Queue: cola de "cuarentena" para mensajes fallidos.
    // NO tiene x-dead-letter-exchange propio para evitar loops infinitos de DLQ → DLX → DLQ.
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    // Conecta la DLQ al DLX con routing key "#" (acepta cualquier clave).
    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("#");
    }

    // Cola de consumo para el evento PaymentFailed.
    // x-dead-letter-exchange: si el consumer falla los 3 reintentos,
    // el mensaje va automáticamente al DLX en lugar de perderse.
    @Bean
    public Queue paymentFailedQueue() {
        return QueueBuilder.durable(PAYMENT_FAILED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE_NAME)
                .build();
    }

    // Binding: suscribe la queue al TopicExchange con la routing key exacta "payment.failed".
    // Cuando Payments publique ese evento al exchange compartido, llegará aquí.
    @Bean
    public Binding paymentFailedBinding(Queue paymentFailedQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(paymentFailedQueue)
                .to(eventsExchange)
                .with(PAYMENT_FAILED_KEY);
    }

    // Jackson converter: mensajes en JSON en lugar de Java serialization binaria.
    // POR QUÉ: interoperabilidad — otros servicios (Python, Node) pueden leer los mensajes.
    // También los mensajes son legibles a simple vista en la RabbitMQ UI.
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // Backoff exponencial: 1s → 2s → 4s entre reintentos (máximo 10s).
    // POR QUÉ exponencial y no fijo: si el servicio dependiente se cae, no lo bombardeamos
    // con 100 reintentos simultáneos en el mismo segundo ("thundering herd").
    // El backoff da tiempo de recuperación y reduce presión sobre el sistema caído.
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
        backOff.setInitialInterval(1000L);  // 1 segundo en el primer reintento
        backOff.setMultiplier(2.0);         // duplica el tiempo en cada fallo
        backOff.setMaxInterval(10000L);     // techo de 10 segundos por intento
        retryTemplate.setBackOffPolicy(backOff);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);      // 1 intento original + 2 reintentos
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

    // Cuando se agotan los reintentos: rechazar el mensaje y NO re-encolar.
    // Esto dispara el mecanismo x-dead-letter-exchange definido en la queue,
    // enviando el mensaje al DLX para inspección manual.
    @Bean
    public MessageRecoverer messageRecoverer() {
        return new RejectAndDontRequeueRecoverer();
    }

    // Fábrica de listeners para todos los @RabbitListener del servicio.
    // defaultRequeueRejected=false: si el listener lanza una excepción no atrapada,
    // el mensaje NO vuelve a la cola automáticamente (evita loops infinitos de reintentos).
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
