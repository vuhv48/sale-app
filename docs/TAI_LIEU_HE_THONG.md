# Tài liệu hệ thống — sale-app (app-platform)

Ứng dụng **modular monolith**: **Spring Boot 4**, **Java 17**, REST API, **JWT** (access) + **refresh token** (opaque, lưu DB), **RBAC**, **PostgreSQL**, **Flyway**, **Spring Batch** (import CSV mẫu). Khóa chính trong CSDL dùng **UUID**; tài khoản và định nghĩa permission/role có cờ **`enabled`**.

---

## 1. Tổng quan kiến trúc

- **Một process chạy** (`bootstrap`), mã tách **theo module Maven** để dễ bảo trì và kiểm tra phụ thuộc (ArchUnit).
- **Luồng chính:** HTTP (`web`) → use case (`application`) → DB (`persistence`); xác thực (`security`) đọc user/quyền qua **port** trong `domain`, triển khai adapter ở `persistence`.
- **Domain** gần như không phụ thuộc Spring; **application** hiện gọi trực tiếp repository/entity JPA (lựa chọn thực dụng; có thể siết cổng sau nếu cần).

---

## 2. Cấu trúc module Maven

| Module | Vai trò |
|--------|---------|
| **common** | `ApiResponse`, `ApiError`, `ErrorStatus`, `UserSummaryResponse`, exception dùng chung |
| **domain** | Port nghiệp vụ (ví dụ `LoadUserForSecurityPort`), snapshot bảo mật (`UserSecuritySnapshot`), value object (ví dụ `StudentCode`) |
| **persistence** | Entity JPA, repository, Flyway (`db/migration`, `R__*.sql`), adapter load user cho Security, `PostgresRoutines` (JdbcTemplate — chỗ tập trung gọi function PostgreSQL sau này) |
| **application** | Interface service (`application.service.*`) + DTO; triển khai `@Service` trong `application.service.impl.*` |
| **security** | Spring Security, JWT (`JwtService`), filter, `AppUserDetails`, `UserProfileReadService` |
| **web** | Controller REST, DTO request + validation, `RestExceptionHandler` |
| **batch** | Job Spring Batch (import sinh viên CSV — tích hợp `StudentImportService`) |
| **bootstrap** | `AppApplication`, `application.yaml`, JAR chạy được; test ArchUnit |

**Phụ thuộc chính (Maven):**

- `bootstrap` → `web`, `batch`, Flyway, Actuator, Tomcat (đảm bảo classpath khi chạy main từ IDE).
- `web` → `application`, `security`, `spring-boot-starter-webmvc`, validation.
- `application` → `domain`, `persistence`, `security`.
- `persistence` → `domain`, JPA, Flyway, PostgreSQL (driver runtime).
- `security` → `domain`, `common`, Spring Security, JJWT.

**Quy ước gọi service:** Code ngoài tầng triển khai chỉ nên phụ thuộc **interface** trong `com.klb.app.application.service…` hoặc `com.klb.app.security.service…`. Implement nằm trong `…service.impl…`.

**REST vs import số lượng lớn:** `StudentService` phục vụ API (list/create), trả DTO (`StudentResponse`, `StudentPageResponse`). Batch gọi `StudentImportService#importIfAbsent` → `ImportedStudentRef` (không lộ entity JPA ra ngoài).

---

## 3. Chạy ứng dụng

### Yêu cầu

- JDK **17+**
- Maven hoặc `./mvnw`
- PostgreSQL, database (mặc định `sale_app`)

### Cấu hình

`bootstrap/src/main/resources/application.yaml`: `spring.datasource.url`, `username`, `password`; giới hạn phân trang (`spring.data.web.pageable.max-page-size`, …).

### Lệnh chạy

```bash
./mvnw -pl bootstrap spring-boot:run
```

Nếu muốn bỏ qua test compile khi build:

```bash
./mvnw -pl bootstrap spring-boot:run -Dmaven.test.skip=true
```

Cổng mặc định **8080** (trừ khi đổi `server.port`).

### Biến môi trường (production)

| Biến | Ý nghĩa |
|------|---------|
| `JWT_SECRET` | Bí mật ký JWT — **bắt buộc đổi** khi triển khai thật (dev có default trong YAML) |

---

## 4. Cơ sở dữ liệu và Flyway

### Vị trí script

- **Versioned:** `persistence/src/main/resources/db/migration/V1__initial_schema.sql` — DDL đầy đủ + **seed dev** (RBAC, user mẫu, sinh viên, một refresh token test).
- **Repeatable:** `R__postgresql_functions.sql` — gom `CREATE OR REPLACE FUNCTION` (chạy lại khi đổi nội dung file).
- **Tay (không Flyway):** `db/manual/rbac_seed.example.sql` — mẫu nạp RBAC qua `psql`/tool.

### Đặc điểm schema (tóm tắt)

- **Khóa chính / khóa ngoại:** kiểu **UUID**, mặc định `gen_random_uuid()` trên các bảng chính.
- **Soft delete:** cột `is_deleted` trên các bảng nghiệp vụ và bảng nối RBAC (`user_roles`, `role_permissions`, `user_permissions`).
- **Bật/tắt định nghĩa RBAC:** bảng **`permissions`** và **`roles`** có thêm **`enabled`** (khác `is_deleted`: vẫn giữ bản ghi catalog).
- **Tài khoản:** bảng `users` có **`enabled`** (Spring Security — khóa/mở user).

### Reset môi trường dev

Khi đổi migration hoặc cần schema sạch: xóa database và tạo lại, hoặc:

`DROP SCHEMA public CASCADE; CREATE SCHEMA public;` (+ cấp quyền user kết nối).

Sau đó chạy app — Flyway áp lại từ đầu.

### Dữ liệu mẫu (dev)

- User **`admin`** / **`user`** — mật khẩu: **`password`** (BCrypt trong SQL).
- Refresh token thử (admin): chuỗi thô **`dev-test-refresh-token-001`** (trong DB là SHA-256 hex; sau khi refresh thành công token bị rotate, không dùng lại).

---

## 5. Bảo mật

### 5.1. JWT (access token)

- Header: `Authorization: Bearer <accessToken>`.
- Claims tiêu biểu:
  - `sub`: username
  - `uid`: **chuỗi UUID** của user (client parse `UUID` từ string nếu cần)
  - `authorities`: permission + role (role có prefix `ROLE_` trong `AppUserDetails`)
- TTL: `app.security.jwt.expiration-seconds`

### 5.2. Refresh token

- Không phải JWT: chuỗi ngẫu nhiên; client giữ bản gốc, server lưu **SHA-256 hex** trong `refresh_tokens`.
- `POST /api/auth/refresh` — mỗi lần thành công **xoay** refresh (revoke bản cũ, cấp cặp token mới).
- Đổi mật khẩu hoặc khóa user (admin) → revoke refresh theo logic `AuthAccountService`.

### 5.3. RBAC và cách tính quyền hiệu lực

- **Permission** (ví dụ): `ADMIN_ACCESS`, `USER_PROFILE_READ`, `STUDENT_READ`, `STUDENT_CREATE`.
- **Role** (ví dụ): `ADMIN`, `USER` — gắn permission qua `role_permissions`; user gắn role qua `user_roles`; quyền trực tiếp qua `user_permissions`.
- Khi build snapshot cho Security, persistence dùng **native SQL** có điều kiện:
  - `is_deleted = false` trên user, role, permission và **cả các bảng nối**;
  - `enabled = true` trên **role** và **permission** (định nghĩa đang bật).
- Đăng ký tự phục vụ: gán role `USER` chỉ khi `findByCodeAndIsDeletedFalseAndEnabledTrue` tìm thấy role.

### 5.4. Permit-all (không cần JWT)

Cấu hình `app.security.permit-all` trong `application.yaml` (method + path), ví dụ: login, register, refresh, `/api/health`, `/actuator/health`. Các route còn lại mặc định **cần authenticated**.

---

## 6. API HTTP

**Base:** `http://localhost:8080` (tuỳ cổng). Body JSON dùng `Content-Type: application/json`.

### 6.1. Auth

| Method | Path | Auth | Mô tả |
|--------|------|------|--------|
| POST | `/api/auth/register` | Không | Đăng ký; role `USER`, `data_scope` `OWN`; **201** + token |
| POST | `/api/auth/login` | Không | Đăng nhập; **200** + token |
| POST | `/api/auth/refresh` | Không | Body `{ "refreshToken": "..." }`; **200** + token mới |
| POST | `/api/auth/change-password` | Bearer JWT | Body `currentPassword`, `newPassword`; **204**; revoke mọi refresh của user |

Validation đăng ký: `username` 3–64 ký tự, `password` 8–128 ký tự.

Response login / register / refresh (ví dụ):

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "tokenType": "Bearer",
  "expiresInSeconds": 86400,
  "refreshExpiresInSeconds": 604800
}
```

*(Một số endpoint auth trả object trực tiếp, không bọc `ApiResponse`.)*

### 6.2. User hiện tại

| Method | Path | Auth | Mô tả |
|--------|------|------|--------|
| GET | `/api/users/current` | Bearer JWT | `id` (**UUID**), `username`, `dataScope`, `roles`, `permissions` |

### 6.3. Sinh viên

| Method | Path | Quyền | Mô tả |
|--------|------|--------|--------|
| GET | `/api/students` | `STUDENT_READ` | Phân trang: `page`, `size`, `sort`; mặc định `size=20`, sort `studentCode`; `size` tối đa theo cấu hình (vd. 100) |
| POST | `/api/students` | `STUDENT_CREATE` | Body `studentCode`, `fullName`; **201** |

Ví dụ: `GET /api/students?page=0&size=10&sort=studentCode,asc`

Response phân trang: `content` (mảng có `id` kiểu UUID), `page`, `size`, `totalElements`, `totalPages`, `first`, `last`.

### 6.4. Admin — tài khoản

| Method | Path | Quyền | Mô tả |
|--------|------|--------|--------|
| POST | `/api/admin/users/{username}/lock` | `ADMIN_ACCESS` | `users.enabled = false`, revoke refresh |
| POST | `/api/admin/users/{username}/unlock` | `ADMIN_ACCESS` | `users.enabled = true` |

### 6.5. Batch

| Method | Path | Quyền | Mô tả |
|--------|------|--------|--------|
| POST | `/api/batch/jobs/student-csv-import` | `ADMIN_ACCESS` | Kích hoạt job import CSV mẫu |

### 6.6. Health & demo

| Method | Path | Ghi chú |
|--------|------|---------|
| GET | `/api/health` | Không JWT |
| GET | `/api/demo/admin` | Cần `ADMIN_ACCESS` |
| GET | `/api/demo/user-role` | Cần role `USER` |
| GET | `/actuator/health` | Actuator (thường permit-all trong YAML) |

---

## 7. Định dạng lỗi API

Nhiều lỗi được map về dạng:

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

`error.code` khớp enum **`ErrorStatus`** (module `common`). Một số response thành công (auth, phân trang) có thể là **JSON thuần** — FE cần phân biệt theo từng endpoint.

---

## 8. Gợi ý tích hợp frontend

- Lưu `accessToken` và `refreshToken` an toàn theo nền tảng.
- Gửi `Authorization: Bearer <accessToken>` cho API được bảo vệ.
- Khi **401**: nếu còn refresh hợp lệ → gọi `/api/auth/refresh`, cập nhật **cả hai** token, retry (tránh vòng lặp vô hạn).
- Claim `uid` là **string UUID** — parse bằng thư viện UUID của FE nếu cần.

---

## 9. Kiểm thử kiến trúc (ArchUnit)

Trong module `bootstrap`, `ModuleArchitectureTest` kiểm tra (tóm tắt):

- `web` không phụ thuộc `batch`.
- `security` không phụ thuộc `persistence`.
- `application` không phụ thuộc `web`.
- `web` không phụ thuộc package `application.service.impl`.
- `batch` không phụ thuộc `web`.
- `domain` không phụ thuộc `persistence`.

---

## 10. Môi trường và vận hành

- Tài liệu và seed trong repo chỉ phù hợp **dev / thử nghiệm**.
- **Production:** đổi `JWT_SECRET`, mật khẩu DB, xem xét tách migration chỉ schema vs dữ liệu, tắt hoặc hạn chế Actuator public, harden CORS và TLS theo chính sách tổ chức.

---

*Tài liệu phản ánh codebase tại thời điểm cập nhật. Khi đổi contract API, schema DB hoặc luồng bảo mật, nên chỉnh các mục 4–7 tương ứng.*
