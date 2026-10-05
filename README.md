# Bank Transfer Hub

Banking transaction hub covering **Pix**, **TED** and **card payments** (debit and credit),
built as a study project with TDD, hexagonal architecture and event-driven design.

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
