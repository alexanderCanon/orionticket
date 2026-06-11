# Guion — Video de 1 minuto: OrionTicket

Duración total: **60 segundos**. Slides: `presentacion-video-1min.md` (Marp).

---

## ⏱️ 0:00 – 0:08 — Slide 1: Título

> "OrionTicket es una plataforma white-label de venta de boletos para eventos masivos, construida con Java 21, Spring Boot 3 y Angular."

## ⏱️ 0:08 – 0:18 — Slide 2: El problema

> "El reto no es solo vender boletos: es hacerlo bajo alta demanda sin sobreventa, con pagos trazables, boletos emitidos una sola vez y validación de acceso con QR donde el primer escaneo gana."

## ⏱️ 0:18 – 0:32 — Slide 3: Arquitectura

> "Usamos microservicios desde el día uno: Identity, Event Management, Seating, Orders, Payments, Ticket Issuance, Access Control, Notifications y Reporting. Cada servicio es dueño exclusivo de sus datos."

## ⏱️ 0:32 – 0:42 — Slide 4: Principios técnicos

> "Cada servicio sigue arquitectura hexagonal, tiene su propia base PostgreSQL con Flyway, y se comunica de forma asíncrona con RabbitMQ. Todo pasa por un API Gateway, con observabilidad completa desde el día uno con Grafana."

## ⏱️ 0:42 – 0:55 — Slide 5: Demo (grabación de pantalla)

> "Veamos el flujo completo: el organizador publica un evento, el comprador reserva un asiento, paga, recibe su boleto con QR dinámico, y en la puerta el sistema valida con semántica first-scan-wins."

**Acción en pantalla:** mostrar frontend Angular — selección de asiento → pago → boleto con QR → escaneo validado.

## ⏱️ 0:55 – 1:00 — Slide 6: Cierre

> "OrionTicket: venta de boletos confiable a escala. Gracias."

---

## Cómo generar las slides

Con la extensión **Marp for VS Code** o Marp CLI:

```bash
npx @marp-team/marp-cli docs/project/presentacion-video-1min.md -o presentacion.html
# o exportar a PDF
npx @marp-team/marp-cli docs/project/presentacion-video-1min.md --pdf
```

## Consejos de grabación

- Slides 1–4: pantalla completa de la presentación (~40 s).
- Slide 5: cambiar a grabación del frontend en vivo (~13 s) — tener el stack levantado antes (`docker compose up --build`).
- Slide 6: regresar a la presentación para cerrar.
- Ritmo: ~150 palabras por minuto; el guion completo tiene ~150 palabras.
