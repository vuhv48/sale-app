# Phan quyen he thong

## 1) Muc tieu

Tai lieu nay mo ta day du co che phan quyen hien tai, gom:
- RBAC theo permission (bang goc cua he thong)
- Dynamic authorization theo resource (route-level)
- Danh sach API lien quan phan quyen de frontend/admin UI tich hop

## 2) Mo hinh du lieu phan quyen

Schema: `authz`

### 2.1 RBAC permission-based (giu nguyen)

- `authz.roles`
  - role catalog (`ADMIN`, `USER`, ...)
- `authz.permissions`
  - `code` (duy nhat)
  - `permission_group`, `action_code` (tach ngu nghia)
- `authz.user_roles`
  - map user -> role
- `authz.role_permissions`
  - map role -> permission
- `authz.user_permissions`
  - override user-level (`effect_type = GRANT|DENY`)

### 2.2 Dynamic resource-based

- `authz.resources`
  - `resource_code` (duy nhat)
  - `resource_group`, `action_code`
  - `url_pattern`, `http_method`
- `authz.role_resources`
  - map role -> resource

### 2.3 Admin console phu tro

- `authz.admin_menus`
- `authz.role_menus`
- `authz.admin_login_logs`

## 3) Luong xac thuc va phan quyen

1. User login qua `/api/auth/login`, nhan JWT.
2. JWT filter xac thuc va dat `Authentication`.
3. `RequestAuthorizationInterceptor` goi `AuthorizationService.assertRequestAccess(method, path)`.
4. `SpringAuthorizationService`:
   - tim resource phu hop (`url_pattern`, `http_method`)
   - lay resource duoc cap qua role cua user
   - neu khong duoc cap -> `403`
5. Chinh sach mac dinh:
   - `app.authz.dynamic.default-deny=true`
   - request khong map resource -> deny

## 4) Danh sach API phan quyen (quan tri)

Base path: `/api/admin/authz`

### 4.1 Quan ly resource

- `GET /api/admin/authz/resources`
  - muc dich: list resource cho man hinh quan tri
- `POST /api/admin/authz/resources`
  - muc dich: tao resource moi
  - body:
    - `resourceCode`
    - `resourceGroup`
    - `actionCode`
    - `name`
    - `urlPattern`
    - `httpMethod`

### 4.2 Gan/quang bo role-resource

- `POST /api/admin/authz/roles/{roleCode}/resources/{resourceCode}`
  - muc dich: cap quyen resource cho role
- `DELETE /api/admin/authz/roles/{roleCode}/resources/{resourceCode}`
  - muc dich: thu hoi quyen resource khoi role

## 5) API lien quan phan quyen (khong thuoc /admin/authz)

### 5.1 Admin user control

- `POST /api/admin/users/{username}/lock`
- `POST /api/admin/users/{username}/unlock`

### 5.2 Auth va audit login

- `POST /api/auth/login`
  - goi `recordAdminLoginSuccess(...)`
  - hien tai ghi vao `authz.admin_login_logs`

## 6) API nghiep vu dang duoc bao ve bang dynamic authz

Vi du seed mac dinh:
- `GET /api/students` -> resource `STUDENT_API_READ`
- `POST /api/students` -> resource `STUDENT_API_CREATE`

Neu endpoint moi chua map vao `authz.resources`, voi `default-deny=true` se bi chuyen `403`.

## 7) Quy uoc dat ma quyen

### 7.1 Permission

- Nen dat theo cap:
  - `permission_group`: module/chuc nang (`STUDENT`, `ORDER`, `USER_PROFILE`)
  - `action_code`: hanh dong (`READ`, `CREATE`, `UPDATE`, `DELETE`, `APPROVE`, ...)

### 7.2 Resource

- `resource_group`: module API
- `action_code`: muc dich route
- `resource_code`: key ky thuat (khong doi tuy tien)

## 8) Khuyen nghi cho he thong lon

- Dung `resources` lam runtime gate o edge/request layer
- Giu `permissions` cho business semantics va reporting
- Chi cho phep merge endpoint moi khi da co mapping resource
- Duy tri script seed/chuan hoa mapping de onboarding nhanh

## 9) Truy van kiem tra nhanh

```sql
select * from authz.roles;
select * from authz.permissions;
select * from authz.role_permissions;
select * from authz.user_permissions;
select * from authz.resources;
select * from authz.role_resources;
select * from authz.admin_login_logs order by logged_in_at desc;
```

