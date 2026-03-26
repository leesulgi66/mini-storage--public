package com.ministorage.mini_s3.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class SignedUrlResponse {

    private String url;
    private String method;
    private OffsetDateTime expiresAt;
}