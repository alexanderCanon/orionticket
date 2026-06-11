---
marp: true
theme: default
paginate: false
backgroundColor: "#0d1117"
color: "#e8f4fd"
style: |
  section {
    font-family: 'Segoe UI', sans-serif;
    padding: 60px;
  }
  h1 { color: #4a90d9; }
  h2 { color: #4caf7d; }
  strong { color: #ffb74d; }
  code { background: #1e3a5f; color: #e8f4fd; }
---

<!-- _class: lead -->

# 🎫 OrionTicket

### Plataforma white-label de venta de boletos para eventos masivos

Java 21 · Spring Boot 3 · Angular 17 · PostgreSQL · RabbitMQ

---

## El problema

- Vender boletos bajo **alta demanda** sin sobreventa
- Reservas de asientos **consistentes**
- Pagos **trazables**, boletos emitidos **una sola vez**
- Validación de acceso con QR: **first-scan-wins**

**v1 sin reembolsos ni reventa** — control operativo primero

---

## Arquitectura: microservicios desde el día 1

| Servicio | Responsabilidad |
|---|---|
| Identity | Usuarios, JWT, roles |
| Event Management | Eventos, venues, catálogo |
| Seating / Inventory | Asientos, reservas |
| Orders / Payments | Órdenes, pagos idempotentes |
| Ticket Issuance | Emisión y QR dinámico |
| Access Control | Validación en puerta |
| Notifications / Reporting | Avisos y proyecciones |

---

## Principios técnicos

- **Arquitectura hexagonal**: `domain / application / infrastructure`
- **Una base de datos por servicio** (PostgreSQL + Flyway)
- Comunicación asíncrona con **RabbitMQ** (consumers idempotentes + DLQ)
- **API Gateway** (Spring Cloud Gateway) + Eureka + Config Server
- **Observabilidad desde el día 1**: Grafana Alloy → Prometheus, Loki, Tempo
- Despliegue multi-VPS con **Docker Compose** y malla **Tailscale**

---

## Demo: flujo completo de compra

1. Organizador crea evento, venue y precios
2. Comprador **reserva un asiento**
3. Paga la orden → se **emite el boleto**
4. Recibe notificación con su **QR dinámico**
5. En puerta: validación **first-scan-wins** ✅

---

<!-- _class: lead -->

# Gracias

**OrionTicket** — venta de boletos confiable a escala

`docker compose up --build` 🚀
