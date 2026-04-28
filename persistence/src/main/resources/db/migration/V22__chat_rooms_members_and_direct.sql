-- Extend chat to support both GROUP and DIRECT conversations via room membership.

ALTER TABLE chat_rooms
    ADD COLUMN room_type VARCHAR(16) NOT NULL DEFAULT 'GROUP',
    ADD COLUMN direct_key VARCHAR(80);

ALTER TABLE chat_rooms
    ADD CONSTRAINT ck_chat_rooms_type CHECK (room_type IN ('GROUP', 'DIRECT'));

CREATE UNIQUE INDEX uq_chat_rooms_direct_key
    ON chat_rooms (direct_key)
    WHERE direct_key IS NOT NULL;

CREATE TABLE chat_room_members (
    room_id      UUID        NOT NULL REFERENCES chat_rooms (id) ON DELETE CASCADE,
    user_id      UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    is_deleted   BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by   VARCHAR(255),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by   VARCHAR(255),
    PRIMARY KEY (room_id, user_id)
);

CREATE INDEX idx_chat_room_members_user ON chat_room_members (user_id, room_id);

-- Ensure existing 'general' room works immediately for current users.
INSERT INTO chat_room_members (room_id, user_id, created_by, updated_by)
SELECT r.id, u.id, 'system', 'system'
FROM chat_rooms r
JOIN users u ON u.is_deleted = false
WHERE r.code = 'general' AND r.is_deleted = false
ON CONFLICT (room_id, user_id) DO NOTHING;

INSERT INTO authz.resources (resource_code, resource_group, action_code, name, url_pattern, http_method)
VALUES ('CHAT_API_DIRECT_OPEN', 'CHAT', 'CREATE', 'Open/create direct chat room', '/api/chat/direct-rooms', 'POST')
ON CONFLICT (resource_code) DO NOTHING;

INSERT INTO authz.resources (resource_code, resource_group, action_code, name, url_pattern, http_method)
VALUES ('CHAT_API_GROUP_CREATE', 'CHAT', 'CREATE', 'Create group chat room', '/api/chat/group-rooms', 'POST')
ON CONFLICT (resource_code) DO NOTHING;

INSERT INTO authz.role_resources (role_id, resource_id)
SELECT r.id, ar.id
FROM authz.roles r
JOIN authz.resources ar ON ar.resource_code IN ('CHAT_API_DIRECT_OPEN', 'CHAT_API_GROUP_CREATE')
WHERE r.code IN ('ADMIN', 'USER')
ON CONFLICT (role_id, resource_id) DO NOTHING;
