package com.ministorage.image_service.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageUploadRequest {

    private String bucket;
    private String key;
    private String acl;             // "public" | "private"
}