# OrionTicket: Estado del Proyecto (Session Memory)

Este archivo mantiene el contexto actual del análisis y diseño del sistema **OrionTicket** para asegurar la continuidad entre sesiones.

## 🚀 Estado Actual
- **Fase:** 4 (Generación de Artefactos Técnicos).
- **Enfoque:** Sistema de misión crítica para venta de entradas híbridas (Stock y Asientos nominales).

## ✅ Completado
1.  **Análisis de Negocio & NFRs:** Definidos en `docs/01-analisis-y-especificaciones.md`.
    - Glosario de dominio (TTL, Outbox, Incidencias).
    - 50k peticiones concurrentes como meta de rendimiento.
    - Historias de usuario para el Sprint 1 (MVP).
2.  **Lógica de Flujo (Secuencia):** Definida en `docs/02-diagramas-de-secuencia.md`.
    - Happy Path con Redis Locks y Kafka.
    - Flujo de limpieza por TTL (5 minutos).
    - Manejo de inconsistencias críticas (Post-cobro).

## 🛠 Próximos Pasos (Pendientes)
1.  **Modelo de Datos (ERD):** Diseñar las tablas para PostgreSQL que soporten el inventario híbrido.
2.  **Diagrama de Arquitectura:** Mapear la interacción de los 5 microservicios, API Gateway, Redis y Kafka.
3.  **Especificación de API:** Definir los endpoints principales para el Comprador y el Administrador.

## 📌 Decisiones Clave
- **TTL de Reserva:** 5 minutos estrictos.
- **Consistencia:** Prioridad absoluta sobre la disponibilidad en caso de sobreventa (Locks en Redis).
- **Manejo de Errores:** Fila de espera manual en el Dashboard Admin para fallos post-pago.
- **Frontend:** Se requieren dos aplicaciones (Dashboard Admin y Sitio de Ventas).
