package com.ministorage.mini_s3.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UploadRequest {

    private String bucket;
    private String key;
    private String contentType;
    private String acl;            // "public" | "private"
    private boolean overwrite;
    private Long contentLength;
}