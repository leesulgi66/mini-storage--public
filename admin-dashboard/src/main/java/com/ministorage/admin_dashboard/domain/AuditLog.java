package com.ministorage.admin_dashboard.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class AuditLog {

    private Long id;
    private String adminId;
    private String action;
    private String target;
    private String ip;
    private OffsetDateTime createdAt;
}