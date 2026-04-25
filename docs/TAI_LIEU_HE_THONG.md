# Tai lieu he thong - sale-app

Tai lieu nay mo ta base hien tai de team vao du an nhanh: cach chay local, module, migration, API va cac nguyen tac van hanh.

Tai lieu phan quyen chi tiet:
- `docs/PHAN_QUYEN_HE_THONG.md`

## 1) Tong quan ky thuat

- Java 17, Spring Boot 4
- Modular monolith (Maven multi-module)
- PostgreSQL + Flyway
- Spring Security + JWT (access/refresh)
- Optional infrastructure: Redis, Kafka, MongoDB, Batch

## 2) Cau truc module

- `bootstrap`: entrypoint app, profile/config
- `web`: controller + request DTO
- `application`: service interface + implementation
- `persistence`: entity/repository + Flyway SQL
- `security`: JWT filter, user details, auth service
- `common`: response, exception, status code
- `redis`, `kafka`, `mongodb`, `batch`: ha tang/tinh nang bo sung

## 3) Migration hien tai

Thu tu migration chinh:
- `V1__initial_schema.sql`: schema nen + authz + seed co ban
- `V2__notices.sql`: notices
- `V3__integration_outbox.sql`: outbox
- `V4__mail.sql`: mail template/queue
- `V10__sales_core.sql`: customers/products/orders MVP
- `V11__customers_rls.sql`: setup runtime DB user + RLS/data_scope cho customers

Luu y:
- He thong dung soft delete (`is_deleted`) o nhieu bang.
- Bang phan quyen nam trong schema `authz`.
- Neu DB local roi voi Flyway history, reset schema/DB roi migrate lai tu dau.

## 4) Chay local

Yeu cau:
- JDK 17+
- PostgreSQL local, database `sale_app`

Lenh:

```bash
./mvnw -pl bootstrap spring-boot:run
```

Mac dinh:
- profile `local`
- port `8080`

## 5) Datasource va Flyway (local)

Profile local da tach 2 account:
- Runtime query (`spring.datasource.*`): `sale_app_user` (khong superuser) de RLS co hieu luc.
- Migration (`spring.flyway.*`): `postgres` (owner) de co quyen ALTER TABLE / CREATE POLICY.

Neu runtime dung `postgres` (superuser), RLS se bi bypass.

## 6) API tong quan

Auth:
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/change-password`

Current user:
- `GET /api/users/current`

Student:
- `GET /api/students`
- `POST /api/students`

Sales MVP:
- `GET /api/customers`
- `POST /api/customers`
- `PATCH /api/customers/{customerId}`
- `GET /api/products`
- `POST /api/products`
- `PATCH /api/products/{productId}`
- `POST /api/products/{productId}/skus`
- `PATCH /api/products/skus/{skuId}`
- `GET /api/orders`
- `GET /api/orders/{orderId}`
- `POST /api/orders`
- `PATCH /api/orders/{orderId}/confirm`
- `PATCH /api/orders/{orderId}/cancel`

Admin:
- `POST /api/admin/users/{username}/lock`
- `POST /api/admin/users/{username}/unlock`
- `GET /api/admin/authz/resources`
- `POST /api/admin/authz/resources`
- `POST /api/admin/authz/roles/{roleCode}/resources/{resourceCode}`
- `DELETE /api/admin/authz/roles/{roleCode}/resources/{resourceCode}`

Health/demo:
- `GET /api/health`
- `GET /actuator/health`
- `/api/demo/*`

## 7) Quy trinh reset local khuyen nghi

Khi can reset tu dau:

```sql
DROP SCHEMA IF EXISTS authz CASCADE;
DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;
```

Sau do start app de Flyway migrate lai.

## 8) Van hanh an toan

- Moi endpoint moi trong production phai map vao `authz.resources` neu dung dynamic authz.
- Khong cho runtime app dung DB superuser.
- Khi doi migration da tung apply tren DB that, khong sua noi dung migration cu; tao version moi.

## 9) Seed dev co san

Trong `V1` da co:
- role: `ADMIN`, `USER`
- user mau: `admin`, `user`
- permission/resource co ban cho luong demo/student

Tai lieu seed tay:
- `persistence/src/main/resources/db/manual/rbac_seed.example.sql`
