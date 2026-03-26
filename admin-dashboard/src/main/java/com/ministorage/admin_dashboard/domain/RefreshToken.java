package com.ministorage.admin_dashboard.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class RefreshToken {

    private String id;
    private String adminId;
    private String tokenHash;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(this.expiresAt);
    }
}