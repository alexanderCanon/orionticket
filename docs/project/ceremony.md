# Sesión de Alineación de Equipo (Kick-off)

La documentación es internamente consistente, pero **no ha sido probada por el equipo**. Todo fue producido en una sesión de diseño intensa. Si los otros 3 miembros comienzan a programar sin revisar estos documentos, descubrirás malentendidos en el Sprint 2 (cuando falle la integración), que es el momento más costoso para corregir.

---

## Qué necesita validación específica del equipo

1.  **Contratos de Servicio:** Ivan debe confirmar que el request/response de `/v1/reservations` funciona para su estrategia de bloqueo. David debe confirmar que el evento `OrderCreated` tiene todo lo que Pagos necesita. Estas son las superficies de integración donde los desajustes causan retrabajo en cadena.
2.  **Límites de Agregados:** La decisión de poner el `Batch` dentro del agregado de `Seat` (ADR-005) tiene consecuencias de implementación reales. Ivan debe entender el porqué antes de escribir la entidad JPA.
3.  **Diagramas ER vs Necesidades Reales:** Los diagramas son lógicos. El equipo probablemente querrá añadir índices, ajustar tipos de columnas o descubrir campos faltantes al pensar en las consultas reales.
4.  **Definition of Done:** Si el equipo no está de acuerdo con el DoD antes del Sprint 1, cada revisión de PR se convertirá en una negociación.

---

## Recomendación: Una única sesión estructurada

Una sesión de **2 horas**, con las 4 personas, antes del Día 1 de la semana de setup:

| Tiempo | Actividad |
|---|---|
| 0:00–0:20 | Lectura individual: `INDEX.md` -> Glosario -> Sus diagramas ER asignados. |
| 0:20–0:40 | Revisión de `service-contracts.md`: solo endpoints cruzados (reservas, órdenes, webhooks de pagos, consulta de tickets para QR). |
| 0:40–1:00 | Revisión de `event-schemas.md`: solo la cadena crítica (`ReservationCreated` -> `OrderCreated` -> `PaymentAuthorized` -> `TicketIssued`). |
| 1:00–1:20 | Revisión del **Definition of Done**. Acordar o ajustar. |
| 1:20–1:40 | Revisión de **TEAM.md** (Ownership). Confirmar que cada persona acepta su alcance. |
| 1:40–2:00 | Identificar ambigüedades. Crear lista de preguntas abiertas y resolverlas. |

**Tras la sesión:** Cualquier ajuste se commitea y entonces comienza el Día 1 de la semana de setup.

---

## Conclusión

La semana de setup ya incluye una revisión técnica el Día 7, pero **es demasiado tarde**. Para el Día 7, la gente ya habrá hecho el scaffolding y las migraciones basadas en su propia interpretación. 

**Mover esta revisión antes del Día 1 no es un retraso; es una inversión que evita el retrabajo que mata los cronogramas de 45 días.**