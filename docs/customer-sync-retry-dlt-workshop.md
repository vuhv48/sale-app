# Customer Sync Retry/DLT Workshop

Goal: understand and validate retry/DLT behavior of `customer-sync` consumer in this project.

## 1) Current architecture (quick recap)

- Listener: `CustomerSyncKafkaListener`
- DB writer: `CustomerSyncUpsertService`
- Error handler: `KafkaListenerDlqConfiguration` (`DefaultErrorHandler` + `DeadLetterPublishingRecoverer`)
- Topic:
  - Main: `app-platform.sales.customer-sync`
  - DLT: `app-platform.sales.customer-sync.DLT`

## 2) Error taxonomy (what should happen)

### A. Parse error (invalid JSON)
- Example: malformed JSON payload.
- Listener throws in `parse(...)`.
- Expected:
  1) retry by `DefaultErrorHandler`
  2) exhausted retries -> message to DLT
  3) DLT headers include `kafka_dlt-exception-*`

### B. Data integrity / schema violation
- Example: `customerCode` longer than DB column length (32).
- `bulkUpsert` fails, fallback `upsertOne` fails, listener throws.
- Expected:
  1) retry
  2) exhausted retries -> DLT with error headers

### C. Validation error in service
- Example: missing `name` / `customerCode` -> `DomainException`.
- Expected:
  1) retry
  2) exhausted retries -> DLT

### D. Infra transient error (DB down)
- Example: stop PostgreSQL during consume.
- Expected:
  1) retry attempts
  2) if DB recovers in time -> success
  3) if not -> DLT

## 3) Prerequisites

1. Kafka + app running
2. `APP_KAFKA_ENABLED=true`
3. Topic exists:
   - `app-platform.sales.customer-sync`
   - `app-platform.sales.customer-sync.DLT`

## 4) Run scenario script

Use script:

```bash
./tools/customer_sync_retry_scenarios.sh --scenario valid
./tools/customer_sync_retry_scenarios.sh --scenario invalid_json
./tools/customer_sync_retry_scenarios.sh --scenario too_long_customer_code
./tools/customer_sync_retry_scenarios.sh --scenario missing_name
```

Default topic: `app-platform.sales.customer-sync`

## 5) Verify outcomes

### Main success path

```sql
select customer_code, name, updated_at
from customers
where customer_code like 'SCN%';
```

### DLT check

Use Kafka UI or CLI to inspect topic:

`app-platform.sales.customer-sync.DLT`

Check headers:
- `kafka_dlt-exception-fqcn`
- `kafka_dlt-exception-message`
- `kafka_dlt-original-topic`
- `kafka_dlt-original-offset`

## 6) Notes for production behavior

- Current config retries with fixed backoff in `KafkaListenerDlqConfiguration`.
- Retry only works when exception is thrown before `ack.acknowledge()`.
- If code catches and swallows exception, framework retry/DLT will not trigger.
