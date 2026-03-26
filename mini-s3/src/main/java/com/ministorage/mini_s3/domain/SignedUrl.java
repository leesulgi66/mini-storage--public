package com.ministorage.mini_s3.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class SignedUrl {

    private String id;
    private String token;
    private String bucket;
    private String key;
    private String method;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(this.expiresAt);
    }
}