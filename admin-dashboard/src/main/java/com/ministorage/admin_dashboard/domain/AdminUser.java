package com.ministorage.admin_dashboard.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUser {

    private String id;
    private String email;
    private String passwordHash;
    private String role;
    private OffsetDateTime createdAt;
}