-- =============================================================================
-- FILE: V1__initial_schema.sql
-- PURPOSE (EN):
-- - Bootstrap the full base schema from an empty database.
-- - Create core domain tables (users, students, refresh_tokens).
-- - Keep original RBAC tables (permissions, role_permissions, user_permissions)
--   and extend with mall-style resource/menu authorization tables.
-- - Seed minimum development data so login and API tests can run immediately.
-- MUC DICH:
-- - Khoi tao schema nen cho toan bo he thong tu database trong.
-- - Tao bang domain cot loi (users, students, refresh_tokens).
-- - Giu nguyen 6 bang RBAC goc (co permissions, role_permissions, user_permissions)
--   va mo rong them resource/menu authz de dap ung bai toan hien tai.
-- - Nap du lieu seed dev toi thieu de co the login/test ngay sau migrate.
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS authz;

CREATE TABLE authz.roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(64) NOT NULL UNIQUE,
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(255),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(255)
);

-- PURPOSE (EN): Master list of fine-grained permissions used by hasAuthority checks.
-- MUC DICH: Danh muc quyen chi tiet phuc vu kiem tra hasAuthority trong service/controller.
CREATE TABLE authz.permissions (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code              VARCHAR(128) NOT NULL UNIQUE,
    permission_group  VARCHAR(64)  NOT NULL,
    action_code       VARCHAR(32)  NOT NULL,
    enabled           BOOLEAN      NOT NULL DEFAULT TRUE,
    is_deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by        VARCHAR(255),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by        VARCHAR(255)
);
CREATE UNIQUE INDEX uq_authz_permissions_group_action ON authz.permissions (permission_group, action_code);

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username        VARCHAR(64)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    data_scope      VARCHAR(128),
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by      VARCHAR(255),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by      VARCHAR(255)
);

CREATE TABLE authz.user_roles (
    user_id     UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id     UUID NOT NULL REFERENCES authz.roles (id) ON DELETE CASCADE,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(255),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(255),
    PRIMARY KEY (user_id, role_id)
);

-- PURPOSE (EN): Role-to-permission mapping table (many-to-many).
-- MUC DICH: Bang lien ket role voi permission de tinh quyen hieu luc theo role.
CREATE TABLE authz.role_permissions (
    role_id       UUID NOT NULL REFERENCES authz.roles (id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES authz.permissions (id) ON DELETE CASCADE,
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    VARCHAR(255),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by    VARCHAR(255),
    PRIMARY KEY (role_id, permission_id)
);

-- PURPOSE (EN): Direct user-permission overrides (GRANT or DENY).
-- MUC DICH: Bang gan quyen truc tiep cho user de override quyen tinh tu role.
CREATE TABLE authz.user_permissions (
    user_id       UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES authz.permissions (id) ON DELETE CASCADE,
    effect_type   VARCHAR(16)  NOT NULL DEFAULT 'GRANT',
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    VARCHAR(255),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by    VARCHAR(255),
    PRIMARY KEY (user_id, permission_id)
);

CREATE TABLE authz.resources (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource_code   VARCHAR(128) NOT NULL UNIQUE,
    resource_group  VARCHAR(64)  NOT NULL,
    action_code     VARCHAR(32)  NOT NULL,
    name            VARCHAR(255) NOT NULL,
    url_pattern     VARCHAR(255) NOT NULL,
    http_method     VARCHAR(16),
    is_enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by      VARCHAR(255),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by      VARCHAR(255)
);
CREATE UNIQUE INDEX uq_authz_resources_group_action_method_path
    ON authz.resources (resource_group, action_code, http_method, url_pattern);

CREATE TABLE authz.role_resources (
    role_id      UUID NOT NULL REFERENCES authz.roles (id) ON DELETE CASCADE,
    resource_id  UUID NOT NULL REFERENCES authz.resources (id) ON DELETE CASCADE,
    is_deleted   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   VARCHAR(255),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by   VARCHAR(255),
    PRIMARY KEY (role_id, resource_id)
);

CREATE TABLE authz.admin_menus (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id    UUID REFERENCES authz.admin_menus (id),
    menu_code    VARCHAR(64)  NOT NULL UNIQUE,
    title        VARCHAR(128) NOT NULL,
    route_path   VARCHAR(255),
    icon         VARCHAR(128),
    hidden       BOOLEAN      NOT NULL DEFAULT FALSE,
    sort_order   INT          NOT NULL DEFAULT 0,
    is_enabled   BOOLEAN      NOT NULL DEFAULT TRUE,
    is_deleted   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   VARCHAR(255),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by   VARCHAR(255)
);

CREATE TABLE authz.role_menus (
    role_id      UUID NOT NULL REFERENCES authz.roles (id) ON DELETE CASCADE,
    menu_id      UUID NOT NULL REFERENCES authz.admin_menus (id) ON DELETE CASCADE,
    is_deleted   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   VARCHAR(255),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by   VARCHAR(255),
    PRIMARY KEY (role_id, menu_id)
);

CREATE TABLE authz.admin_login_logs (
    id           BIGSERIAL PRIMARY KEY,
    user_id      UUID REFERENCES users (id) ON DELETE SET NULL,
    username     VARCHAR(64) NOT NULL,
    ip_address   VARCHAR(64),
    user_agent   VARCHAR(255),
    logged_in_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE students (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_code  VARCHAR(32)  NOT NULL UNIQUE,
    full_name     VARCHAR(255) NOT NULL,
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    VARCHAR(255),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by    VARCHAR(255)
);

-- PURPOSE (EN): Store hashed refresh tokens for JWT rotation, revocation, and expiry control.
-- MUC DICH: Luu refresh token da bam hash de xoay token, thu hoi token va kiem soat han su dung.
CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash  VARCHAR(64)  NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_students_student_code ON students (student_code);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_user_permissions_user_id ON authz.user_permissions (user_id);
CREATE INDEX idx_user_permissions_permission_id ON authz.user_permissions (permission_id);
CREATE INDEX idx_authz_resources_lookup ON authz.resources (url_pattern, http_method, is_enabled);
CREATE INDEX idx_authz_role_resources_role_id ON authz.role_resources (role_id);
CREATE INDEX idx_authz_role_resources_resource_id ON authz.role_resources (resource_id);
CREATE INDEX idx_authz_admin_menus_parent ON authz.admin_menus (parent_id, sort_order);
CREATE INDEX idx_authz_role_menus_role ON authz.role_menus (role_id);
CREATE INDEX idx_authz_role_menus_menu ON authz.role_menus (menu_id);
CREATE INDEX idx_authz_admin_login_logs_user ON authz.admin_login_logs (user_id, logged_in_at DESC);
CREATE INDEX idx_authz_admin_login_logs_time ON authz.admin_login_logs (logged_in_at DESC);

COMMENT ON COLUMN users.data_scope IS 'Phạm vi dữ liệu (VD: ALL, BRANCH:01, OWN) — đọc từ /api/me';

-- =============================================================================
-- Dữ liệu test (dev) — mật khẩu đăng nhập admin/user: password
-- Refresh token thô (gửi POST /api/auth/refresh): dev-test-refresh-token-001
-- =============================================================================

-- authz.roles
INSERT INTO authz.roles (code) VALUES ('ADMIN') ON CONFLICT (code) DO NOTHING;
INSERT INTO authz.roles (code) VALUES ('USER') ON CONFLICT (code) DO NOTHING;

-- authz.permissions
INSERT INTO authz.permissions (code, permission_group, action_code) VALUES ('ADMIN_ACCESS', 'ADMIN', 'ACCESS') ON CONFLICT (code) DO NOTHING;
INSERT INTO authz.permissions (code, permission_group, action_code) VALUES ('USER_PROFILE_READ', 'USER_PROFILE', 'READ') ON CONFLICT (code) DO NOTHING;
INSERT INTO authz.permissions (code, permission_group, action_code) VALUES ('STUDENT_READ', 'STUDENT', 'READ') ON CONFLICT (code) DO NOTHING;
INSERT INTO authz.permissions (code, permission_group, action_code) VALUES ('STUDENT_CREATE', 'STUDENT', 'CREATE') ON CONFLICT (code) DO NOTHING;

-- users (BCrypt của chữ "password")
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

-- authz.user_roles
INSERT INTO authz.user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN authz.roles r ON r.code = 'ADMIN' WHERE u.username = 'admin'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO authz.user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN authz.roles r ON r.code = 'USER' WHERE u.username = 'user'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- authz.role_permissions
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

-- authz.user_permissions (override mau): user "user" duoc grant STUDENT_CREATE
INSERT INTO authz.user_permissions (user_id, permission_id, effect_type)
SELECT u.id, p.id, 'GRANT'
FROM users u
JOIN authz.permissions p ON p.code = 'STUDENT_CREATE'
WHERE u.username = 'user'
ON CONFLICT (user_id, permission_id) DO NOTHING;

-- authz.resources
INSERT INTO authz.resources (resource_code, resource_group, action_code, name, url_pattern, http_method)
VALUES ('STUDENT_API_READ', 'STUDENT', 'READ', 'Read student API', '/api/students/**', 'GET')
ON CONFLICT (resource_code) DO NOTHING;

INSERT INTO authz.resources (resource_code, resource_group, action_code, name, url_pattern, http_method)
VALUES ('STUDENT_API_CREATE', 'STUDENT', 'CREATE', 'Create student API', '/api/students', 'POST')
ON CONFLICT (resource_code) DO NOTHING;

-- authz.role_resources
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

-- authz.admin_menus + authz.role_menus
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

INSERT INTO authz.role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM authz.roles r
JOIN authz.admin_menus m ON m.menu_code IN ('ADMIN_ROOT', 'ADMIN_USERS', 'ADMIN_AUTHZ')
WHERE r.code = 'ADMIN'
ON CONFLICT (role_id, menu_id) DO NOTHING;

-- students
INSERT INTO students (student_code, full_name)
VALUES ('SV2024001', 'Nguyễn Văn An')
ON CONFLICT (student_code) DO NOTHING;

INSERT INTO students (student_code, full_name)
VALUES ('SV2024002', 'Trần Thị Bình')
ON CONFLICT (student_code) DO NOTHING;

INSERT INTO students (student_code, full_name)
VALUES ('SV2024003', 'Lê Minh Cường')
ON CONFLICT (student_code) DO NOTHING;

-- refresh_tokens (SHA-256 hex của chuỗi UTF-8: dev-test-refresh-token-001)
INSERT INTO refresh_tokens (user_id, token_hash, expires_at, revoked)
SELECT u.id,
       '7d534154c286239185abf6b56a469d2b9152a5de4f0557dc9c92d59a69d886af',
       now() + interval '7 days',
       FALSE
FROM users u
WHERE u.username = 'admin'
ON CONFLICT (token_hash) DO NOTHING;
