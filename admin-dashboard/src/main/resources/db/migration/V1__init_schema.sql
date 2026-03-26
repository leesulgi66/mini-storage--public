CREATE TABLE admin_users
(
    id            VARCHAR(30) PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE refresh_tokens
(
    id         VARCHAR(30) PRIMARY KEY,
    admin_id   VARCHAR(30)  NOT NULL REFERENCES admin_users (id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_tokens_admin_id ON refresh_tokens (admin_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);

CREATE TABLE audit_logs
(
    id         BIGSERIAL PRIMARY KEY,
    admin_id   VARCHAR(30)  REFERENCES admin_users (id) ON DELETE SET NULL,
    action     VARCHAR(100) NOT NULL,
    target     TEXT,
    ip         VARCHAR(45),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_logs_admin_id ON audit_logs (admin_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at DESC);

-- 초기 관리자 계정 (비밀번호: admin1234)
-- 운영 배포 전 반드시 변경할 것
INSERT INTO admin_users (id, email, password_hash)
VALUES ('adm_00000000000000000001',
        'admin@example.com',
        '$2a$12$HjVXxBMGaFX3EBz4mLhRuOFlVNNlCFbF5GV/QvfYm3oV7q2bABiHy');