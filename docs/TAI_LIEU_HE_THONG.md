# Tai lieu he thong - sale-app

## 1) Muc tieu

Tai lieu nay mo ta nhanh base hien tai de team co the:
- dung dung ung dung ngay sau khi clone
- hieu module va luong xu ly chinh
- tra cuu API va quy trinh van hanh co ban

Tai lieu phan quyen duoc tach rieng tai:
- `docs/PHAN_QUYEN_HE_THONG.md`

## 2) Cong nghe chinh

- Java 17, Spring Boot 4
- PostgreSQL + Flyway
- Spring Security + JWT (access token + refresh token)
- Modular monolith theo Maven multi-module
- Tuy chon: Redis, Kafka, MongoDB, Batch

## 3) Cau truc module

- `bootstrap`: diem chay app, config chung
- `web`: controller + DTO request/response
- `application`: use case/service implementation
- `persistence`: JPA entity/repository, migration SQL
- `security`: JWT, user details, security config
- `domain`: port + model domain khong phu thuoc framework
- `common`: response/error dung chung
- `redis`, `kafka`, `mongodb`, `batch`: module ha tang/tinh nang tuy chon

## 4) Database va migration

Migration dang dung:
- `V1__initial_schema.sql`: schema nen + RBAC + dynamic authz + seed dev
- `V2__notices.sql`: bang notices
- `V3__integration_outbox.sql`: transactional outbox
- `V4__mail.sql`: template mail + queue mail

Luu y:
- He thong dung soft-delete (`is_deleted`) o nhieu bang
- Phan quyen nam trong schema `authz`
- Neu dung lai DB cu ma Flyway loi baseline, reset schema cho sach roi chay lai

## 5) Chay local

Yeu cau:
- JDK 17+
- PostgreSQL local (mac dinh `sale_app`)

Lenh:

```bash
./mvnw -pl bootstrap spring-boot:run
```

Mac dinh:
- port `8080`
- profile `local`

## 6) API chinh (tong quan)

Auth:
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/change-password`

Nguoi dung hien tai:
- `GET /api/users/current`

Sinh vien:
- `GET /api/students`
- `POST /api/students`

Admin user:
- `POST /api/admin/users/{username}/lock`
- `POST /api/admin/users/{username}/unlock`

Admin authz resource:
- `GET /api/admin/authz/resources`
- `POST /api/admin/authz/resources`
- `POST /api/admin/authz/roles/{roleCode}/resources/{resourceCode}`
- `DELETE /api/admin/authz/roles/{roleCode}/resources/{resourceCode}`

Batch:
- `POST /api/batch/jobs/student-csv-import`

Health/demo:
- `GET /api/health`
- cac endpoint demo trong `/api/demo/*`

## 7) Bao mat tong quan

- Tat ca request (tru permit-all) phai authenticated JWT
- Dynamic authz check theo bang `authz.resources` + `authz.role_resources`
- Chinh sach mac dinh:
  - `app.authz.dynamic.default-deny=true`
  - endpoint khong map resource se bi deny

Chi tiet day du ve phan quyen:
- xem `docs/PHAN_QUYEN_HE_THONG.md`

## 8) Seed du lieu dev

Tu dong seed trong `V1`:
- role: `ADMIN`, `USER`
- permission: `ADMIN_ACCESS`, `USER_PROFILE_READ`, `STUDENT_READ`, `STUDENT_CREATE`
- resource mau cho student read/create

Tai lieu seed tay:
- `persistence/src/main/resources/db/manual/rbac_seed.example.sql`

## 9) Ghi chu van hanh

- Neu them API moi trong production, can dang ky vao `authz.resources`
- Khuyen nghi quan ly mapping API-quyen qua quy trinh review
- Log login admin dang ghi vao `authz.admin_login_logs` (can kiem soat role neu muon strict admin-only)

# Tai lieu he thong — sale-app (app-platform)

Ung dung **modular monolith** tren **Spring Boot 4** va **Java 17**: REST API, **JWT** (access) + **refresh token** (opaque, luu PostgreSQL), **RBAC**, **Flyway**, **Spring Batch** (import CSV mau), **Redis** (tuy chon), **Apache Kafka** (tuy chon). Khoa chinh CSDL kieu **UUID**; bang `permissions`, `roles`, `users` co cot **`enabled`**.

---

## 1. Tong quan kien truc

- **Mot JVM / mot artifact runnable** (`bootstrap`); ma chia **module Maven** va **ArchUnit** giu ranh gioi package.
- **Luong HTTP:** `web` → interface `application.service.*` → trien khai `application.service.impl.*` → `persistence` (JPA / native SQL).
- **Bao mat:** `security` (JWT, filter, `AppUserDetails`); du lieu user/quyen lay qua **`LoadUserForSecurityPort`** trong `domain`, adapter **`LoadUserForSecurityJpaAdapter`** trong `persistence` (package `com.klb.app.persistence.security`, khac module `security`).
- **Domain** gan nhu khong phu thuoc Spring; **application** hien dung truc tiep repository/entity JPA (co the chat che hon sau).

---

## 2. Cau truc module Maven

| Module | Vai tro |
|--------|---------|
| **common** | `ApiResponse`, `ErrorStatus`, `UserSummaryResponse`, exception dung chung |
| **domain** | Port (`LoadUserForSecurityPort`), `UserSecuritySnapshot`, value object (vd. `StudentCode`) |
| **persistence** | Entity, repository, Flyway, adapter Security, `PostgresRoutines` (JdbcTemplate / function PostgreSQL) |
| **application** | Service interface + DTO; `@Service` trong `service.impl`; demo Redis (`RedisCounterDemoService`) |
| **security** | Spring Security, JWT, `AppUserDetails`, `UserProfileReadService` |
| **redis** | Ha tang Redis: Lettuce, `StringRedisTemplate`, bean `redisJsonTemplate`, `RedisKeyFactory`, `SaleRedisAutoConfiguration` |
| **kafka** | Ha tang Kafka: gate `SaleKafkaAutoConfiguration`, import Boot `KafkaAutoConfiguration` + `KafkaMetricsAutoConfiguration`, `KafkaTopicFactory`, `KafkaInfrastructureProperties` |
| **web** | Controller, DTO request, `RestExceptionHandler` |
| **batch** | Spring Batch + `StudentImportService` |
| **bootstrap** | `AppApplication`, `application.yaml`, fat JAR; test H2 + ArchUnit |

**Phu thuoc chinh:**

- `bootstrap` → `web`, `batch`, **`redis`**, **`kafka`**, Flyway, Actuator, Tomcat.
- `web` → `application`, `security`, WebMVC, validation.
- `application` → `domain`, `persistence`, `security`, **`redis`** (chua phu thuoc **`kafka`**; them module `kafka` vao `application` khi can `KafkaTemplate` / listener trong tang use-case).
- `persistence` → `domain`, JPA, Flyway, PostgreSQL (runtime).
- `security` → `domain`, `common`, Spring Security, JJWT.
- **`redis`** → starter Data Redis + JSON (khong phu thuoc `web` / `persistence` / `application` / `batch`).
- **`kafka`** → `spring-boot-starter-kafka` (khong phu thuoc `web` / `persistence` / `application` / `batch`).

**Quy uoc:** Code ben ngoai tang trien khai chi import **`com.klb.app.application.service...`** hoac **`com.klb.app.security.service...`**; implement trong **`...service.impl...`**.

---

## 3. Chay ung dung

### Yeu cau

- JDK **17+**
- `./mvnw` hoac Maven
- PostgreSQL, database mac dinh **`sale_app`**

### Cau hinh chinh

File **`bootstrap/src/main/resources/application.yaml`:**

- `spring.datasource.*`, JPA, Flyway
- `spring.data.redis.*` (host / port / timeout — dung khi bat Redis)
- `spring.kafka.*` (bootstrap, producer/consumer String serializer, acks, idempotence, listener `manual_immediate` — dung khi bat Kafka)
- `spring.autoconfigure.exclude`: `DataRedisAutoConfiguration`, `KafkaAutoConfiguration`, `KafkaMetricsAutoConfiguration`
- `app.redis.enabled`, `app.redis.key-prefix`
- `app.kafka.enabled`, `app.kafka.topic-prefix`
- `app.security.*` (JWT, permit-all)
- Phan trang: `spring.data.web.pageable.*`

### Lenh

```bash
./mvnw -pl bootstrap spring-boot:run
```

Bo qua compile test:

```bash
./mvnw -pl bootstrap spring-boot:run -Dmaven.test.skip=true
```

Cong mac dinh **8080**.

### Bien moi truong (goi y)

| Bien | Y nghia |
|------|---------|
| `JWT_SECRET` | Ky JWT — **doi tren production** |
| `APP_REDIS_ENABLED` | `true` / `false` — bat Redis (mac dinh false) |
| `REDIS_HOST`, `REDIS_PORT` | Ket noi Redis (mac dinh localhost:6379) |
| `APP_KAFKA_ENABLED` | `true` / `false` — bat Kafka beans (mac dinh false) |
| `KAFKA_BOOTSTRAP_SERVERS` | Broker (mac dinh localhost:9092) |
| `KAFKA_CLIENT_ID` | Client id producer/consumer (mac dinh `spring.application.name`) |
| `KAFKA_CONSUMER_GROUP_ID` | Group consumer (mac dinh `{spring.application.name}-consumer`) |
| `KAFKA_PRODUCER_RETRIES` | So lan retry producer (mac dinh 3) |

---

## 4. Redis (tuy chon)

### Co che

- **`spring.autoconfigure.exclude`:** `org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration` (Spring Boot 4) — Spring khong tu tao client mac dinh.
- Khi **`app.redis.enabled=true`** hoac **`APP_REDIS_ENABLED=true`**, module **`redis`** nap **`SaleRedisAutoConfiguration`**: **Lettuce**, **`StringRedisTemplate`**, **`redisJsonTemplate`** (JSON), **`RedisKeyFactory`** (tien to tu `app.redis.key-prefix`).
- Service co the inject **`ObjectProvider<StringRedisTemplate>`** / **`ObjectProvider<RedisKeyFactory>`** de app van chay khi Redis tat.

### Demo

- **`GET /api/demo/redis-counter`** (permit-all): moi lan goi **INCR** key `app-platform:demo:global-counter`, TTL ~24 gio.
- Response: `{"redisAvailable": true|false, "value": <so hoac null}`.

Vi du:

```bash
docker run -d -p 6379:6379 redis:7-alpine
APP_REDIS_ENABLED=true ./mvnw -pl bootstrap spring-boot:run
curl -s http://localhost:8080/api/demo/redis-counter
```

---

## 5. Kafka (tuy chon)

### Co che

- **`spring.autoconfigure.exclude`:** `org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration`, `org.springframework.boot.kafka.autoconfigure.metrics.KafkaMetricsAutoConfiguration` — khong dang ky Kafka khi chua bat.
- Khi **`app.kafka.enabled=true`** hoac **`APP_KAFKA_ENABLED=true`**, module **`kafka`** nap **`SaleKafkaAutoConfiguration`**: `@Import` lai hai lop auto-config tren + bean **`KafkaTopicFactory`** (ten topic: `app.kafka.topic-prefix` + cac doan `.segment...`).
- Cau hinh broker / serializer / consumer: **`spring.kafka.*`** trong YAML (mac dinh **String** serializer/deserializer; doi sang JSON/Avro trong YAML khi can).

### Goi y tich hop code

- Them dependency **`kafka`** vao module **`application`** (hoac module publish event), inject **`KafkaTemplate`**, dung **`KafkaTopicFactory.topic("domain", "event")`** de dat ten topic dong bo.
- **`@KafkaListener`:** dat o tang phu hop (thuong `application` hoac module rieng), `ack-mode` manual da cau hinh trong YAML.

Vi du chay local (can broker):

```bash
# Vi du: da co Kafka / Redpanda / docker-compose listening :9092
APP_KAFKA_ENABLED=true ./mvnw -pl bootstrap spring-boot:run
```

---

## 6. Co so du lieu va Flyway

### Script

- **`V1__initial_schema.sql`:** DDL + seed dev (RBAC, user, student, refresh token mau).
- **`R__postgresql_functions.sql`:** repeatable — function/procedure PostgreSQL.
- **`db/manual/rbac_seed.example.sql`:** mau nap tay (khong Flyway).

### Schema (tom tat)

- PK/FK **UUID**, `gen_random_uuid()` noi can.
- **Soft delete:** `is_deleted` (entity + bang noi RBAC).
- **`permissions.enabled` / `roles.enabled`:** bat/tat dinh nghia catalog.
- **`users.enabled`:** khoa/mo tai khoan (Spring Security).

### Reset dev

Xoa DB hoac `DROP SCHEMA public CASCADE; CREATE SCHEMA public;` roi chay lai app (Flyway tu dau).

### Seed tham chieu

- User **`admin`** / **`user`**, mat khau **`password`**.
- Refresh thu: **`dev-test-refresh-token-001`** (sau refresh thanh cong thi khong dung lai).

---

## 7. Bao mat

### JWT

- Header: `Authorization: Bearer <token>`
- `sub` = username; **`uid`** = **UUID dang chuoi**; `authorities` = permission + `ROLE_*`.

### Refresh token

- Opaque; server luu SHA-256; rotate moi lan `/api/auth/refresh`; doi mat khau / khoa user → revoke refresh.

### RBAC (hieu luc)

- Native SQL: `is_deleted = false` (user, role, permission, bang noi) va `enabled = true` (role + permission).
- Dang ky: gan role qua **`findByCodeAndIsDeletedFalseAndEnabledTrue`**.

### Permit-all

Cau hinh **`app.security.permit-all`:** login, register, refresh, `/api/health`, `/actuator/health`, **`GET /api/demo/redis-counter`**. Con lai mac dinh **authenticated**.

---

## 8. API HTTP

Base: `http://localhost:8080`. Body JSON: `Content-Type: application/json`.

### Auth

| Method | Path | Auth |
|--------|------|------|
| POST | `/api/auth/register` | Khong |
| POST | `/api/auth/login` | Khong |
| POST | `/api/auth/refresh` | Khong |
| POST | `/api/auth/change-password` | JWT |

Dang ky: username 3–64 ky tu, password 8–128 ky tu.

### User hien tai

| Method | Path | Auth |
|--------|------|------|
| GET | `/api/users/current` | JWT |

### Sinh vien

| Method | Path | Quyen |
|--------|------|--------|
| GET | `/api/students` | `STUDENT_READ` (phan trang `page`, `size`, `sort`) |
| POST | `/api/students` | `STUDENT_CREATE` |

### Admin

| Method | Path | Quyen |
|--------|------|--------|
| POST | `/api/admin/users/{username}/lock` | `ADMIN_ACCESS` |
| POST | `/api/admin/users/{username}/unlock` | `ADMIN_ACCESS` |
| GET | `/api/admin/authz/resource-categories` | `ADMIN_ACCESS` |
| POST | `/api/admin/authz/resource-categories` | `ADMIN_ACCESS` |
| GET | `/api/admin/authz/resources` | `ADMIN_ACCESS` |
| POST | `/api/admin/authz/resources` | `ADMIN_ACCESS` |
| POST | `/api/admin/authz/roles/{roleCode}/resources/{resourceCode}` | `ADMIN_ACCESS` |
| DELETE | `/api/admin/authz/roles/{roleCode}/resources/{resourceCode}` | `ADMIN_ACCESS` |
| PUT | `/api/admin/authz/users/{username}/resources/{resourceCode}` | `ADMIN_ACCESS` |
| DELETE | `/api/admin/authz/users/{username}/resources/{resourceCode}` | `ADMIN_ACCESS` |

### Authz runtime (resource-level)

- Bang: `authz_resource_categories`, `authz_resources`, `authz_role_resources`, `authz_user_resources`.
- Match request theo `http_method + url_pattern` (`AntPathMatcher`).
- Thu tu quyet dinh:
  - direct user `DENY` > direct user `GRANT` > role-resource grant.
  - endpoint khong map trong `authz_resources` thi bo qua (de rollout dan).
- File test nhanh: `http/admin-authz.http`.

### Authz admin-console (mall-like)

- Bang tuong duong mall da co:
  - `users` ~ `ums_admin`
  - `roles` ~ `ums_role`
  - `user_roles` ~ `ums_admin_role_relation`
  - `authz_resources` ~ `ums_resource`
  - `authz_role_resources` ~ `ums_role_resource_relation`
  - `authz_resource_categories` ~ `ums_resource_category`
  - `permissions` + metadata cay (parent/type/uri/sort) ~ `ums_permission`
  - `role_permissions` ~ `ums_role_permission_relation`
  - `user_permissions` (co `effect_type`) ~ `ums_admin_permission_relation`
  - `authz_admin_menus` ~ `ums_menu`
  - `authz_role_menus` ~ `ums_role_menu_relation`
  - `authz_admin_login_logs` ~ `ums_admin_login_log`

### Batch

| Method | Path | Quyen |
|--------|------|--------|
| POST | `/api/batch/jobs/student-csv-import` | `ADMIN_ACCESS` |

### Health va demo

| Method | Path | Ghi chu |
|--------|------|---------|
| GET | `/api/health` | Public |
| GET | `/api/demo/redis-counter` | Public — demo Redis |
| GET | `/api/demo/admin` | `ADMIN_ACCESS` |
| GET | `/api/demo/user-role` | role `USER` |
| GET | `/actuator/health` | Public (theo YAML) |

---

## 9. Dinh dang loi API

Nhieu loi qua `RestExceptionHandler`:

```json
{
  "success": false,
  "data": null,
  "error": {
    "timestamp": "...",
    "status": 401,
    "code": "REFRESH_TOKEN_INVALID",
    "message": "...",
    "path": "/api/auth/refresh",
    "details": {}
  }
}
```

`error.code` khop enum **`ErrorStatus`** (`common`). Mot so response thanh cong la JSON thuan (auth, phan trang, demo Redis).

---

## 10. Goi y tich hop frontend

- Luu access + refresh an toan; gui `Authorization: Bearer ...`.
- 401 → thu refresh, cap nhat ca hai token, retry co gioi han.
- Parse **`uid`** (chuoi UUID) neu can.

---

## 11. Kiem thu

- **`bootstrap`**, profile **`test`:** H2 in-memory, Flyway tat, **`app.redis.enabled: false`**, **`app.kafka.enabled: false`** (`application-test.yaml`).
- **ArchUnit** (`ModuleArchitectureTest`): dung package day du `com.klb.app.<module>..` (tranh nham `persistence.security` voi module `security`):
  - `com.klb.app.web..` khong phu thuoc `com.klb.app.batch..`
  - `com.klb.app.security..` khong phu thuoc `com.klb.app.persistence..`
  - `com.klb.app.application..` khong phu thuoc `com.klb.app.web..`
  - `com.klb.app.web..` khong phu thuoc `com.klb.app.application.service.impl..`
  - `com.klb.app.batch..` khong phu thuoc `com.klb.app.web..`
  - `com.klb.app.domain..` khong phu thuoc `com.klb.app.persistence..`
  - `com.klb.app.redis..` khong phu thuoc web / persistence / application / batch
  - `com.klb.app.kafka..` khong phu thuoc web / persistence / application / batch
- Spring Boot 4 — test MVC: `@AutoConfigureMockMvc` trong **`org.springframework.boot.webmvc.test.autoconfigure`**.

---

## 12. Moi truong production

Doi **`JWT_SECRET`**, credential DB, can nhac **Redis** va **Kafka** (cluster, ACL, idempotent producer), thu hep Actuator, TLS/CORS theo chinh sach.

---

*Tai lieu dong bo codebase; khi doi API, Flyway, Redis hoac Kafka, cap nhat muc 3–8 va 11 neu can.*
