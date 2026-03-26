CREATE TABLE objects
(
    id           UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    bucket       VARCHAR(63)  NOT NULL,
    key          TEXT         NOT NULL,
    etag         VARCHAR(64)  NOT NULL,
    size         BIGINT       NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    acl          VARCHAR(10)  NOT NULL DEFAULT 'private'
        CHECK (acl IN ('public', 'private')),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (bucket, key)
);

CREATE INDEX idx_objects_bucket ON objects (bucket);
CREATE INDEX idx_objects_bucket_key ON objects (bucket, key);

CREATE TABLE signed_urls
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    token      TEXT        NOT NULL UNIQUE,
    bucket     VARCHAR(63) NOT NULL,
    key        TEXT        NOT NULL,
    method     VARCHAR(6)  NOT NULL CHECK (method IN ('GET', 'PUT')),
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_signed_urls_token ON signed_urls (token);
CREATE INDEX idx_signed_urls_expires_at ON signed_urls (expires_at);