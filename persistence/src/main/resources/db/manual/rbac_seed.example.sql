-- =============================================================================
-- MẪU NẠP DỮ LIỆU 6 BẢNG RBAC (chạy tay — KHÔNG được Flyway tự chạy file này)
-- =============================================================================
-- Thứ tự: permissions → roles → role_permissions → users → user_roles → user_permissions
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

-- 1) permissions — mã quyền khớp @PreAuthorize("hasAuthority('...')")
INSERT INTO permissions (code) VALUES ('ADMIN_ACCESS') ON CONFLICT (code) DO NOTHING;
INSERT INTO permissions (code) VALUES ('USER_PROFILE_READ') ON CONFLICT (code) DO NOTHING;
INSERT INTO permissions (code) VALUES ('STUDENT_READ') ON CONFLICT (code) DO NOTHING;
INSERT INTO permissions (code) VALUES ('STUDENT_CREATE') ON CONFLICT (code) DO NOTHING;

-- 2) roles
INSERT INTO roles (code) VALUES ('ADMIN') ON CONFLICT (code) DO NOTHING;
INSERT INTO roles (code) VALUES ('USER') ON CONFLICT (code) DO NOTHING;

-- 3) role_permissions — role ADMIN: đủ quyền mẫu
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('ADMIN_ACCESS', 'USER_PROFILE_READ', 'STUDENT_READ', 'STUDENT_CREATE')
WHERE r.code = 'ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- role USER: chỉ đọc profile + đọc student
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('USER_PROFILE_READ', 'STUDENT_READ')
WHERE r.code = 'USER'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 4) users — data_scope: ví dụ ALL
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

-- 5) user_roles
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'ADMIN' WHERE u.username = 'admin'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'USER' WHERE u.username = 'user'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- 6) user_permissions — quyền gán THẲNG cho user (tuỳ chọn, thường để trống nếu đủ qua role)
-- Ví dụ: thêm STUDENT_CREATE riêng cho user (bỏ comment nếu cần):
-- INSERT INTO user_permissions (user_id, permission_id)
-- SELECT u.id, p.id FROM users u JOIN permissions p ON p.code = 'STUDENT_CREATE' WHERE u.username = 'user'
-- ON CONFLICT (user_id, permission_id) DO NOTHING;

COMMIT;
