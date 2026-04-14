# ADR-014: Message Broker Selection

| Field | Value |
|---|---|
| **Date** | 2026-04-13 |
| **Status** | ACCEPTED |

## Context

The architecture relies on asynchronous domain event delivery between bounded contexts. A message broker is needed to decouple producers (Seating/Inventory, Orders, Payments, Ticket Issuance, Access Control, Event Management) from consumers (Notifications, Reporting, and reactive flows in other services). The broker must handle up to 1,000 transactions/minute at peak with guaranteed delivery.

## Decision

**RabbitMQ.**

## Justification

- Simpler operation compared to Kafka — no ZooKeeper or KRaft cluster management.
- Lighter infrastructure footprint, well-suited for VPS deployment (ADR-016).
- Sufficient throughput for projected peak load (1,000 txn/min).
- Built-in dead-letter exchange (DLX) for failed message handling.
- Per-queue FIFO ordering satisfies domain event ordering requirements.
- At-least-once delivery with consumer acknowledgments.
- Fits team operational capacity — manageable by 1 DevOps engineer on a VPS.

## Options Considered

## Options to Evaluate

| Option | Ordering | Delivery | Dead-letter | Managed cost | Complexity |
|---|---|---|---|---|---|
| **RabbitMQ** | Per-queue FIFO | At-least-once | Built-in DLX | Low (self-hosted) / Medium (CloudAMQP) | Medium |
| **Apache Kafka** | Per-partition ordered | At-least-once (exactly-once with transactions) | Custom consumer group retry | Medium–High | High |
| **AWS SQS + SNS** | Best-effort (FIFO available) | At-least-once | Built-in DLQ | Pay-per-use (low at start) | Low |
| **Google Pub/Sub** | Best-effort | At-least-once | Built-in DLQ | Pay-per-use (low at start) | Low |

## Evaluation Criteria

1. Ordering guarantees sufficient for domain events (ReservationExpired must not be processed before ReservationCreated).
2. Dead-letter support for failed message processing with retry and manual inspection.
3. Throughput ≥ 1,000 messages/min sustained.
4. Operational cost within budget constraints.
5. Team expertise and learning curve.
6. Compatibility with chosen cloud provider (ADR-016).

## Source

- discovery.md — Block 11.5, Block 11.6
- domain-events-map.md — full event catalog
