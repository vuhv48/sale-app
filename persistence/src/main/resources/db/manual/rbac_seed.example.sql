-- =============================================================================
-- FILE: rbac_seed.example.sql
-- PURPOSE (EN):
-- - Provide sample mall-style RBAC seed data for quick testing.
-- - Use for manual authorization data reset in development environments.
-- - This is a manual script and must not be executed by Flyway automatically.
-- MUC DICH:
-- - Cung cap script seed mau cho RBAC kieu mall de test nhanh.
-- - Su dung khi can nap lai du lieu phan quyen thu cong tren moi truong dev.
-- - Day la file manual, KHONG cho Flyway tu dong chay.
-- MẪU NẠP DỮ LIỆU RBAC + RESOURCE AUTHZ (chạy tay — KHÔNG để Flyway tự chạy file này)
-- =============================================================================
-- Thứ tự:
--   authz.roles -> authz.permissions -> users -> authz.user_roles
--   -> authz.role_permissions -> authz.user_permissions
--   -> authz.resources -> authz.role_resources
--
-- Cách dùng:
--   psql -h localhost -U sale -d sale_app -f rbac_seed.example.sql
--   hoặc copy từng khối vào DBeaver / DataGrip.
--
-- password_hash: phải là BCrypt (Spring Security). Ví dụ dưới tương ứng mật khẩu: password
--   (chỉ để dev; production dùng mật khẩu mạnh + hash riêng).
--
-- data_scope (cột users.data_scope): tuỳ nghiệp vụ, ví dụ:
--   ALL          — toàn bộ dữ liệu được phép theo permission
--   BRANCH:HN    — chỉ chi nhánh (ghi chuẩn nội bộ, code app tự parse)
--   OWN          — chỉ bản ghi của user
-- =============================================================================

BEGIN;

-- 1) authz.roles
INSERT INTO authz.roles (code) VALUES ('ADMIN') ON CONFLICT (code) DO NOTHING;
INSERT INTO authz.roles (code) VALUES ('USER') ON CONFLICT (code) DO NOTHING;

-- 1.1) authz.permissions
INSERT INTO authz.permissions (code, permission_group, action_code) VALUES ('ADMIN_ACCESS', 'ADMIN', 'ACCESS') ON CONFLICT (code) DO NOTHING;
INSERT INTO authz.permissions (code, permission_group, action_code) VALUES ('USER_PROFILE_READ', 'USER_PROFILE', 'READ') ON CONFLICT (code) DO NOTHING;
INSERT INTO authz.permissions (code, permission_group, action_code) VALUES ('STUDENT_READ', 'STUDENT', 'READ') ON CONFLICT (code) DO NOTHING;
INSERT INTO authz.permissions (code, permission_group, action_code) VALUES ('STUDENT_CREATE', 'STUDENT', 'CREATE') ON CONFLICT (code) DO NOTHING;

-- 2) users — data_scope: ví dụ ALL
--    password_hash = BCrypt của chữ: password
INSERT INTO users (username, password_hash, enabled, data_scope)
VALUES (
    'admin',
    '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
    TRUE,
    'ALL'
) ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password_hash, enabled, data_scope)
VALUES (
    'user',
    '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
    TRUE,
    'OWN'
) ON CONFLICT (username) DO NOTHING;

-- 3) authz.user_roles
INSERT INTO authz.user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN authz.roles r ON r.code = 'ADMIN' WHERE u.username = 'admin'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO authz.user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN authz.roles r ON r.code = 'USER' WHERE u.username = 'user'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- 3.1) authz.role_permissions
INSERT INTO authz.role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM authz.roles r
JOIN authz.permissions p ON p.code IN ('ADMIN_ACCESS', 'USER_PROFILE_READ', 'STUDENT_READ', 'STUDENT_CREATE')
WHERE r.code = 'ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO authz.role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM authz.roles r
JOIN authz.permissions p ON p.code IN ('USER_PROFILE_READ', 'STUDENT_READ')
WHERE r.code = 'USER'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 3.2) authz.user_permissions (override mau)
INSERT INTO authz.user_permissions (user_id, permission_id, effect_type)
SELECT u.id, p.id, 'GRANT'
FROM users u
JOIN authz.permissions p ON p.code = 'STUDENT_CREATE'
WHERE u.username = 'user'
ON CONFLICT (user_id, permission_id) DO NOTHING;

-- 4) authz.resources
INSERT INTO authz.resources (resource_code, resource_group, action_code, name, url_pattern, http_method)
VALUES (
    'STUDENT_API_READ',
    'STUDENT',
    'READ',
    'Read student API',
    '/api/students/**',
    'GET'
)
ON CONFLICT (resource_code) DO NOTHING;

INSERT INTO authz.resources (resource_code, resource_group, action_code, name, url_pattern, http_method)
VALUES (
    'STUDENT_API_CREATE',
    'STUDENT',
    'CREATE',
    'Create student API',
    '/api/students',
    'POST'
)
ON CONFLICT (resource_code) DO NOTHING;

-- 5) authz.role_resources
INSERT INTO authz.role_resources (role_id, resource_id)
SELECT r.id, ar.id
FROM authz.roles r
JOIN authz.resources ar ON ar.resource_code IN ('STUDENT_API_READ', 'STUDENT_API_CREATE')
WHERE r.code = 'ADMIN'
ON CONFLICT (role_id, resource_id) DO NOTHING;

INSERT INTO authz.role_resources (role_id, resource_id)
SELECT r.id, ar.id
FROM authz.roles r
JOIN authz.resources ar ON ar.resource_code IN ('STUDENT_API_READ')
WHERE r.code = 'USER'
ON CONFLICT (role_id, resource_id) DO NOTHING;

-- 6) authz.admin_menus + authz.role_menus (menu backend)
INSERT INTO authz.admin_menus (menu_code, title, route_path, sort_order, is_enabled)
VALUES ('ADMIN_ROOT', 'Admin', '/admin', 0, TRUE)
ON CONFLICT (menu_code) DO NOTHING;

INSERT INTO authz.admin_menus (parent_id, menu_code, title, route_path, sort_order, is_enabled)
SELECT root.id, 'ADMIN_USERS', 'Users', '/admin/users', 10, TRUE
FROM authz.admin_menus root
WHERE root.menu_code = 'ADMIN_ROOT'
ON CONFLICT (menu_code) DO NOTHING;

INSERT INTO authz.admin_menus (parent_id, menu_code, title, route_path, sort_order, is_enabled)
SELECT root.id, 'ADMIN_AUTHZ', 'Authorization', '/admin/authz', 20, TRUE
FROM authz.admin_menus root
WHERE root.menu_code = 'ADMIN_ROOT'
ON CONFLICT (menu_code) DO NOTHING;

-- Role ADMIN thay full menu root + submenu
INSERT INTO authz.role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM authz.roles r
JOIN authz.admin_menus m ON m.menu_code IN ('ADMIN_ROOT', 'ADMIN_USERS', 'ADMIN_AUTHZ')
WHERE r.code = 'ADMIN'
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- Role USER chi thay menu root (neu can)
INSERT INTO authz.role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM authz.roles r
JOIN authz.admin_menus m ON m.menu_code IN ('ADMIN_ROOT')
WHERE r.code = 'USER'
ON CONFLICT (role_id, menu_id) DO NOTHING;

COMMIT;
