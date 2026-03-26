CREATE TABLE images
(
    id           VARCHAR(30) PRIMARY KEY,
    object_key   TEXT         NOT NULL UNIQUE,
    bucket       VARCHAR(63)  NOT NULL,
    width        INTEGER,
    height       INTEGER,
    size         BIGINT       NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    acl          VARCHAR(10)  NOT NULL DEFAULT 'public'
        CHECK (acl IN ('public', 'private')),
    etag         VARCHAR(64),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_images_created_at ON images (created_at DESC);
CREATE INDEX idx_images_acl ON images (acl);

CREATE TABLE image_variants
(
    id           VARCHAR(30) PRIMARY KEY,
    image_id     VARCHAR(30) NOT NULL REFERENCES images (id) ON DELETE CASCADE,
    name         VARCHAR(50) NOT NULL,
    object_key   TEXT        NOT NULL,
    width        INTEGER,
    height       INTEGER,
    size         BIGINT,
    content_type VARCHAR(255)         DEFAULT 'image/webp',
    status       VARCHAR(20) NOT NULL DEFAULT 'processing'
        CHECK (status IN ('processing', 'ready', 'failed')),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_image_variants_image_id ON image_variants (image_id);
CREATE INDEX idx_image_variants_status ON image_variants (status);