# Microservicio: Reporting (Reportes)

Responsable de consolidar datos para inteligencia de negocio a través de modelos de lectura optimizados.

---

## 1. Responsabilidades
- Generación de reportes de ventas (Organizadores).
- Reportes de comisiones (Plataforma).
- Reportes de acceso/afluencia (Staff).
- Consolidación de datos históricos.

## 2. Documentación Técnica Específica
- **Casos de Uso:** [UC-RE-01, UC-RE-02, UC-RE-03](../../phases/phase-2/use-case-catalog.md).
- **Modelos de Lectura (Proyecciones):** [SalesReport, CommissionReport, AccessReport](../../phases/phase-1/aggregate-definitions.md).
- **Contrato API:** [Reporting Endpoints](../../phases/phase-3/service-contracts.md).
- **Diagrama ER:** [Modelo de Datos de Reporting](../../phases/phase-3/er-diagrams/reporting.md).

## 3. Funcionamiento
Este servicio es **puramente reactivo**. No genera eventos que cambien el estado del negocio, solo consume eventos para actualizar sus proyecciones (consistencia eventual).

### Consume:
- `TicketIssued`, `PaymentAuthorized`, `PayoutProcessed`, `ValidationSucceeded`, etc.

---

## 4. Estándares Aplicables
- [NFR-030: Consistencia Eventual en Reportes](../../phases/phase-3/non-functional-requirements.md).
