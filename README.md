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

## Cómo ejecutar

```bash
make proto    # genera los stubs en gen/java
make test     # tests unitarios
make run      # requiere PostgreSQL: en beacon-infra, make up
```

Puertos: HTTP `8080` (solo actuator: `/actuator/health`) y gRPC `9090`.

Probar con grpcurl (el servidor expone reflection):

```bash
grpcurl -plaintext localhost:9090 list
grpcurl -plaintext -d '{"title":"DB caída","service":"checkout","severity":"SEVERITY_HIGH"}' \
  localhost:9090 beacon.incidents.v1.IncidentService/CreateIncident
```

## Variables de entorno

| Variable | Por defecto |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5434/incidents` |
| `DB_USER` / `DB_PASSWORD` | `beacon` / `beacon` |
| `GRPC_PORT` | `9090` |

## Estado

Fase 1: CRUD de incidentes, máquina de estados y gRPC. `ReportAlert` responde `UNIMPLEMENTED` hasta la Fase 2.
