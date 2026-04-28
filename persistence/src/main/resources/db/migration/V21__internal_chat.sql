-- Internal chat: rooms + messages (PostgreSQL). Seed kênh chung + quyền đọc API chat cho ADMIN/USER.

CREATE TABLE chat_rooms (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(64)  NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by      VARCHAR(255),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by      VARCHAR(255)
);

CREATE TABLE chat_messages (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id           UUID         NOT NULL REFERENCES chat_rooms (id) ON DELETE CASCADE,
    sender_id         UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    sender_username   VARCHAR(64)  NOT NULL,
    body              TEXT         NOT NULL,
    is_deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by        VARCHAR(255),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by        VARCHAR(255)
);

CREATE INDEX idx_chat_messages_room_created ON chat_messages (room_id, created_at ASC);

INSERT INTO chat_rooms (code, name)
VALUES ('general', 'Kênh chung')
ON CONFLICT (code) DO NOTHING;

INSERT INTO authz.resources (resource_code, resource_group, action_code, name, url_pattern, http_method)
VALUES ('CHAT_API_READ', 'CHAT', 'READ', 'Đọc lịch sử chat', '/api/chat/**', 'GET')
ON CONFLICT (resource_code) DO NOTHING;

INSERT INTO authz.role_resources (role_id, resource_id)
SELECT r.id, ar.id
FROM authz.roles r
JOIN authz.resources ar ON ar.resource_code = 'CHAT_API_READ'
WHERE r.code IN ('ADMIN', 'USER')
ON CONFLICT (role_id, resource_id) DO NOTHING;

COMMENT ON TABLE chat_rooms IS 'Phòng chat nội bộ (mã room dùng trong topic STOMP)';
COMMENT ON TABLE chat_messages IS 'Tin nhắn chat; sender_username denormalized để broadcast nhanh';
