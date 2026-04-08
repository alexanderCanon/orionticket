# Arquitectura de Datos Resiliente para Sistemas de Misión Crítica
Diseño, despliegue y demostración de una plataforma distribuida basada en microservicios.

🎯 Objetivo Principal
Diseñar, desplegar y demostrar una plataforma distribuida basada en microservicios que soporte fallos de infraestructura, alta concurrencia y consistencia de datos, aplicando principios de:

Alta disponibilidad
Escalabilidad horizontal
Tolerancia a fallos
Observabilidad
Automatización
Consistencia eventual
Pruebas de carga y caos

🔥 Prueba Final Obligatoria
Todos los grupos deben demostrar empíricamente que su sistema:

Sobrevive al caos: Sigue operando aunque se eliminen contenedores o pods.
Es consistente: Mantiene las reglas de negocio intactas en todo momento.
Soporta estrés: Tolera al menos 50,000 peticiones en pruebas de carga.
Es confiable: No presenta pérdida de datos críticos ni duplicidad indebida.
Es observable: Expone métricas y evidencia técnica de su comportamiento.

📋 5 Reglas Base Comunes
Estas reglas son obligatorias e iguales para cualquier aplicación elegida.

1. Arquitectura Obligatoria
- API Gateway o punto de entrada único.
- 3 Microservicios de negocio (mínimo).
- PostgreSQL como base de datos principal.
- Redis para caché, rate limiting o sesiones.
- Mensajería asíncrona (Kafka o RabbitMQ).
- Contenedorización con Docker.
- Orquestación: Docker Compose avanzado o Kubernetes (ideal).
- Monitoreo: Prometheus y Grafana.
- CI/CD básico con GitHub Actions.
- Estrategia HA al menos en capa de aplicación.

2. Requisitos de Alta Disponibilidad (HA)
La solución debe ser capaz de tolerar:

- Caída de un contenedor de microservicio.
- Caída de múltiples réplicas con recuperación automática.
- Reinicio del servicio de caché sin colapsar la plataforma.
- Degradación controlada de funcionalidades no críticas.
- Reintentos y recuperación ante errores transitorios.

3. Requisitos de Consistencia
El sistema debe proteger el negocio (no duplicar operaciones, no sobrevender, no procesar pagos dobles, no confirmar reservas inexistentes, no perder eventos). Estrategias esperadas:

- Idempotencia
- Locks lógicos/negocio
- Control de concurrencia
- Colas / Eventos
- Sagas / Compensaciones
- Outbox Pattern

4. Requisitos de Rendimiento
Pruebas a ejecutar: Carga mínima (50k peticiones), concurrente sostenida, picos, y falla inducida.
Métricas a reportar:

- Throughput
- Latencia promedio
- Latencia p95 / p99
- Tasa de error
- Uso de CPU y memoria
- Comportamiento al matar pods/contenedores

5. Caos y Resiliencia Obligatoria (Demostración)
Durante la demo se debe ejecutar al menos una de estas acciones destructivas:

- Eliminar un pod de un microservicio crítico.
- Matar un contenedor manualmente.
- Escalar hacia abajo y volver a escalar.
- Simular caída del consumidor de eventos.
- Reiniciar Redis o un worker secundario.

Se debe demostrar que: El sistema se recupera, no se corrompen los datos y las operaciones críticas se mantienen consistentes.

📦 Entregables Requeridos
💻 Entregables Técnicos
- Repositorio Git del proyecto.
- Diagramas de arquitectura y microservicios.
- Modelo de datos.
- Archivos Docker / Kubernetes.
- Configuración de monitoreo.
- Colección Postman o endpoints documentados.
- Scripts de carga (JMeter / k6).
- Dashboards de Grafana/Prometheus exportados.

📄 Informe Técnico
- Problema de negocio y arquitectura propuesta.
- Estrategia de HA/HP y consistencia.
- Estrategia de caché.
- Comunicación síncrona y asíncrona.
- Resultados de pruebas de carga y caos.
- Incidentes encontrados y mejoras futuras.

📹 Evidencias Obligatorias
- Video corto o demo funcional.
- Capturas de logs.
- Capturas del monitoreo.
- Capturas de prueba de estrés (50k).
- Evidencia en video/captura de caída y recuperación.

## Proyecto a desarrollar
🎟️ Sold-Out Challenge Live

Idea: Plataforma para compra de boletos de eventos masivos con alta concurrencia.

Microservicios: event-service, inventory-service, booking-service, payment-service, notification-service.

Reglas Críticas: No sobrevender asientos, reserva expira sin pago, pago confirma ticket, notificaciones únicas.

Estrés Ideal: Miles de usuarios en un solo evento, caída de pagos, caída de reservas.

Evalúa: Concurrencia, locks, sagas, expiración de reservas, caché de disponibilidad.

## Nombre del sistema "OrionTicket"
La idea menciona una plataforma para compra de boletos de eventos masivos con alta concurrencia, pero vamos a darle un enfoque híbrido, que soporte venta de boletos individuales, por ejemplo para un concierto de música, y boletos o asientos cuando el evento es por mesas con sillas. La diferencia? Tickets se descuentan del stock, en asientos se inserta un registro en reservaciones. 