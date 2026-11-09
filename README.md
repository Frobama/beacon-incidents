# beacon-incidents

Servicio de **incidentes** de Beacon. Es la fuente de verdad del ciclo de vida: `triggered → acknowledged → mitigating → resolved → postmortem`.

| | |
|---|---|
| Lenguaje | Java 21 (Spring Boot) |
| Base de datos | PostgreSQL |
| Expone | gRPC (`IncidentService`) |
| Publica (NATS) | `incident.opened`, `incident.acknowledged`, `incident.resolved`, `incident.severity_changed` |
| Escucha (NATS) | `escalation.triggered`, `notification.sent` |

## Contratos

```bash
make proto
```

Genera en `gen/java` (no versionado). Detalle de subjects en [`beacon-contracts/docs/nats-subjects.md`](https://github.com/Frobama/beacon-contracts/blob/main/docs/nats-subjects.md).

## Entorno local

Base de datos y NATS corren desde [`beacon-infra`](https://github.com/Frobama/beacon-infra) (`make up`):

```text
postgres://beacon:beacon@localhost:5434/incidents
nats://localhost:4222
```

## Estado

Fase 0: solo generación de contratos. El primer corte vertical llega en la Fase 1.
