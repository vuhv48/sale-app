# Phan quyen he thong

Tai lieu nay mo ta co che phan quyen dang dung trong codebase: RBAC + dynamic resource authz + data-level RLS cho `customers`.

## 1) Mo hinh quyen tong quan

He thong hien tai co 3 lop:

- **Lop 1 - Xac thuc:** JWT (`security` module), tao `Authentication`.
- **Lop 2 - Dynamic authz theo API route:** check theo `authz.resources` + `authz.role_resources`.
- **Lop 3 - Data-level authz (RLS):** PostgreSQL RLS tren bang `customers`, dua vao context role + `data_scope`.

## 2) Du lieu authz (schema `authz`)

RBAC co ban:
- `authz.roles`
- `authz.permissions`
- `authz.user_roles`
- `authz.role_permissions`
- `authz.user_permissions` (override user-level)

Dynamic route-based authz:
- `authz.resources` (`resource_code`, `resource_group`, `action_code`, `url_pattern`, `http_method`)
- `authz.role_resources`

Admin console support:
- `authz.admin_menus`
- `authz.role_menus`
- `authz.admin_login_logs`

## 3) Luong check quyen request

1. Login -> nhan JWT.
2. JWT filter parse token, dat `Authentication` voi `AppUserDetails`.
3. Interceptor authz route goi `AuthorizationService.assertRequestAccess(method, path)`.
4. Neu pass route-level, service layer chay.
5. Truoc khi service query DB, `PostgresRlsContextAspect` set context RLS:
   - role
   - username
   - data_scope
6. JPA query den `customers` bi RLS filter tu dong theo policy.

## 4) RLS cho customers (state hien tai)

Migration: `V11__customers_rls.sql`.

### 4.1 DB runtime user

- Runtime user: `sale_app_user` (non-superuser, `NOBYPASSRLS`).
- Flyway user local: `postgres` (owner) de co quyen tao/sua policy.
- Neu runtime dung `postgres`, RLS se bi bypass.

### 4.2 Context function

Function:
- `set_rls_context(p_user_role, p_username, p_data_scope)`

Ben trong function:
- `PERFORM set_config('app.user_role', ...)`
- `PERFORM set_config('app.username', ...)`
- `PERFORM set_config('app.data_scope', ...)`

### 4.3 Policy logic

Policy `customers_scope_policy` cho `customers`:

- `ADMIN` -> thay tat ca
- `data_scope = ALL` -> thay tat ca
- `data_scope = OWN` -> chi thay ban ghi co `created_by = username`
- Nguoc lai -> khong thay

## 5) API quan tri authz

Base: `/api/admin/authz`

- `GET /api/admin/authz/resources`
- `POST /api/admin/authz/resources`
- `POST /api/admin/authz/roles/{roleCode}/resources/{resourceCode}`
- `DELETE /api/admin/authz/roles/{roleCode}/resources/{resourceCode}`

Admin user control:
- `POST /api/admin/users/{username}/lock`
- `POST /api/admin/users/{username}/unlock`

## 6) Quy uoc dat ma

Permission:
- `permission_group` + `action_code`
- Vi du: `STUDENT` + `READ`

Resource:
- `resource_group` + `action_code`
- `resource_code` giu on dinh de map role-resource

## 7) Checklist khi them rule data_scope moi

Moi lan them field context moi (vd: `branch_code`), cap nhat dong bo 3 diem:

1. `PostgresRlsContextAspect`: truyen them tham so xuong DB.
2. Function `set_rls_context(...)`: them tham so + `set_config`.
3. Policy RLS: bo sung dieu kien `current_setting('app.xxx', true)`.

## 8) Truy van debug nhanh

```sql
-- runtime DB user co bypass RLS hay khong
select current_user;
select rolsuper, rolbypassrls from pg_roles where rolname = current_user;

-- customers da bat RLS chua
select relrowsecurity, relforcerowsecurity
from pg_class
where relname = 'customers';

-- policy hien tai tren customers
select polname, cmd, qual, with_check
from pg_policies
where schemaname = 'public' and tablename = 'customers';
```

