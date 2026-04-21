# Resumen: 45 Días para Implementar

**Días 1–7: Semana de Setup**
- Documentación completa (Fases 2, 3, 4).
- El equipo lee todo; no se permiten ambigüedades.
- Entorno local operativo con `docker-compose`.

**Días 8–17: Sprint 1 — Bases**
- Servicio de Identidad (Auth, roles, JWT).
- Gestión de Eventos (Flujo de creación, envío y aprobación).
- Panel Organizador básico (CRUD de eventos).

**Días 18–27: Sprint 2 — Transacción Core**
- Inventario y Asientos (Control de asientos, reservas, tandas).
- Pedidos (Checkout, resolución de precios).
- Pagos (Integración con pasarela).

**Días 28–37: Sprint 3 — Ciclo de vida del Boleto**
- Emisión de Boletos (Generación de QR, entrega).
- Notificaciones (Email, SMS básico).
- Portal del Comprador (Ver compras, descargar boletos).

**Días 38–45: Sprint 4 — Operaciones y Estabilización**
- Control de Acceso (Validación en tiempo real).
- Reportes Básicos (Reporte de ventas).
- Corrección de errores, pruebas de integración y preparación de demo.

---

## Qué se recorta de la especificación original (Scope Cuts):

1.  **Sincronización Offline del Validador:** Demo solo con validación en tiempo real.
2.  **Regeneración de QR Dinámico:** QR estático aceptable para el demo del MVP si el tiempo apremia.
3.  **Automatización de Liquidaciones:** Activación manual por parte de Finanzas es suficiente.
4.  **Log de Auditoría Completo:** Registro básico de logs es aceptable.
5.  **Anti-fraude (Rate limiting, Virtual Queue):** Documentar como v2.