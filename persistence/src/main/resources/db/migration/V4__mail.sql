-- =============================================================================
-- FILE: V4__mail.sql
-- PURPOSE (EN):
-- - Create email template and email queue tables.
-- - Support queue-based email flow: PENDING -> SENT/FAILED.
-- - Allow business flows to enqueue emails now and send asynchronously later.
-- MUC DICH:
-- - Tao bang template email va hang doi gui email.
-- - Ho tro flow queue-based mail: PENDING -> SENT/FAILED.
-- - Cho phep nghiep vu dang ky/day thong bao ghi queue truoc, gui that sau.
-- =============================================================================

CREATE TABLE mail_template (
    id               BIGSERIAL PRIMARY KEY,
    code             VARCHAR(64)  NOT NULL UNIQUE,
    subject_template VARCHAR(500) NOT NULL,
    body_html        TEXT         NOT NULL,
    body_text        TEXT,
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE mail_queue (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID REFERENCES users (id) ON DELETE SET NULL,
    to_address       VARCHAR(320) NOT NULL,
    template_code    VARCHAR(64)  NOT NULL REFERENCES mail_template (code),
    variables        JSONB        NOT NULL DEFAULT '{}'::jsonb,
    status           VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
    attempt_count    INT          NOT NULL DEFAULT 0,
    last_error       VARCHAR(2000),
    sent_at          TIMESTAMPTZ,
    idempotency_key  VARCHAR(128) NOT NULL UNIQUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_mail_queue_pending ON mail_queue (status, created_at)
    WHERE status = 'PENDING';

COMMENT ON TABLE mail_template IS 'Noi dung mail theo ma template';
COMMENT ON TABLE mail_queue IS 'Outbox mail: PENDING -> SENT/FAILED (Kafka consumer hoac job gui SMTP)';

INSERT INTO mail_template (code, subject_template, body_html, body_text)
VALUES (
    'WELCOME_REGISTER',
    'Chào mừng {{username}} — tài khoản đã tạo',
    '<p>Xin chào <strong>{{username}}</strong>,</p><p>Tài khoản của bạn đã được tạo tại <em>{{appName}}</em>.</p><p>Bạn có thể đăng nhập bằng tên đăng ký và mật khẩu đã đặt.</p>',
    'Xin chào {{username}}, tài khoản của bạn đã được tạo tai {{appName}}.'
);
