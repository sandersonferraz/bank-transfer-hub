# Bank Transfer Hub

Banking transfer hub is case study that covering **Pix**, **TED** and **card payments** (debit and credit),
built as a study project with TDD, hexagonal architecture, and event-driven design.

## Overview
The project is a banking transfer hub that processes PIX/TED transfers using eventual consistency flow.

```

POST /transfers
   │
   ├─ cria a ORDEM (Transfer) PENDING, depois grava o evento no OUTBOX  ──► MongoDB (2 gravações)
   │
   └─ OutboxRelayer publica ──► Kafka transfer.requested ──► antifraude-service
                                                                  │
                                                                  ▼
                            Kafka transfer.verdict ◄── veredito (APPROVED/REJECTED)
                                     │
                                     ▼
                       AUTHORIZE: PENDING ─► APPROVED (ou REJECTED)
                                     │
                                     ▼   (somente se APPROVED)
                       Kafka transfer.approved
                                     │
                                     ▼
                       SETTLE: par DEBIT/CREDIT no ledger + Transfer COMPLETED

```

Three invariants guide the entrie codebase

1. **Noney noves only upon settlement** — autthorization(verdict) and settlement (ledger entries) are separate steps; a `REJECTED` order does not generate a ledger entry.
2. **Indepontency is a core guarantee** — the same `idepotency key` never creates two orders, event under currency (enforced by a unique index in MongoDB).
3. **No dual-writes (MongoDB -> Kafka)** — the oder and the event (outbox) are written to MongoDB in that specific order; a relayer then publishes to kafka.

## Highlights

- **Double-entry ledger**: append-only, with the invariant `Σ debits == Σ credits`
- **Transactional outbox** and **choreographed saga** over Kafka, with Dead Letter Topics
- **Idempotency** enforced by a unique index (sequential and concurrent requests)
- **Asynchronous fraud checks** before settlement
- **Card lifecycle**: authorization, capture, clearing and settlement
- **End-of-day reconciliation** with auditable adjustments
- **Observability**: tracing, structured logs and business metrics



## Stack

Java 21 · Spring Boot · Apache Kafka · MongoDB · Testcontainers · ArchUnit · Docker Compose
