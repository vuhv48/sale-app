-- =============================================================================
-- Schema ban đầu (gộp từ các migration cũ V1–V7).
-- Dùng với CSDL mới hoặc sau khi DROP SCHEMA public CASCADE; CREATE SCHEMA public;
-- =============================================================================

CREATE TABLE permissions (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(128) NOT NULL UNIQUE,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(255),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(255)
);

CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(64) NOT NULL UNIQUE,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(255),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(255)
);

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
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

CREATE TABLE user_roles (
    user_id     BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id     BIGINT       NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(255),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(255),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE role_permissions (
    role_id       BIGINT       NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permission_id BIGINT       NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    VARCHAR(255),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by    VARCHAR(255),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE user_permissions (
    user_id       BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    permission_id BIGINT       NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    VARCHAR(255),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by    VARCHAR(255),
    PRIMARY KEY (user_id, permission_id)
);

CREATE TABLE students (
    id            BIGSERIAL PRIMARY KEY,
    student_code  VARCHAR(32)  NOT NULL UNIQUE,
    full_name     VARCHAR(255) NOT NULL,
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    VARCHAR(255),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by    VARCHAR(255)
);

CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash  VARCHAR(64)  NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_students_student_code ON students (student_code);
CREATE INDEX idx_user_permissions_user_id ON user_permissions (user_id);
CREATE INDEX idx_user_permissions_permission_id ON user_permissions (permission_id);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);

COMMENT ON COLUMN users.data_scope IS 'Phạm vi dữ liệu (VD: ALL, BRANCH:01, OWN) — đọc từ /api/me';

-- =============================================================================
-- Dữ liệu test (dev) — mật khẩu đăng nhập admin/user: password
-- Refresh token thô (gửi POST /api/auth/refresh): dev-test-refresh-token-001
-- =============================================================================

-- permissions
INSERT INTO permissions (code) VALUES ('ADMIN_ACCESS') ON CONFLICT (code) DO NOTHING;
INSERT INTO permissions (code) VALUES ('USER_PROFILE_READ') ON CONFLICT (code) DO NOTHING;
INSERT INTO permissions (code) VALUES ('STUDENT_READ') ON CONFLICT (code) DO NOTHING;
INSERT INTO permissions (code) VALUES ('STUDENT_CREATE') ON CONFLICT (code) DO NOTHING;

-- roles
INSERT INTO roles (code) VALUES ('ADMIN') ON CONFLICT (code) DO NOTHING;
INSERT INTO roles (code) VALUES ('USER') ON CONFLICT (code) DO NOTHING;

-- role_permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('ADMIN_ACCESS', 'USER_PROFILE_READ', 'STUDENT_READ', 'STUDENT_CREATE')
WHERE r.code = 'ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('USER_PROFILE_READ', 'STUDENT_READ')
WHERE r.code = 'USER'
ON CONFLICT (role_id, permission_id) DO NOTHING;

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

-- user_roles
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'ADMIN' WHERE u.username = 'admin'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'USER' WHERE u.username = 'user'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- user_permissions (quyền gán thẳng user — minh họa; user "user" thêm STUDENT_CREATE ngoài role)
INSERT INTO user_permissions (user_id, permission_id)
SELECT u.id, p.id
FROM users u
JOIN permissions p ON p.code = 'STUDENT_CREATE'
WHERE u.username = 'user'
ON CONFLICT (user_id, permission_id) DO NOTHING;

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
