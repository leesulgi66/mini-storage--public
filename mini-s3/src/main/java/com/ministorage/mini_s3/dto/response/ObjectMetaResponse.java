package com.ministorage.mini_s3.dto.response;

import com.ministorage.mini_s3.domain.StorageObject;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class ObjectMetaResponse {

    private String bucket;
    private String key;
    private String etag;
    private long size;
    private String contentType;
    private String acl;
    private OffsetDateTime createdAt;

    public static ObjectMetaResponse from(StorageObject obj) {
        return ObjectMetaResponse.builder()
                .bucket(obj.getBucket())
                .key(obj.getKey())
                .etag(obj.getEtag())
                .size(obj.getSize())
                .contentType(obj.getContentType())
                .acl(obj.getAcl())
                .createdAt(obj.getCreatedAt())
                .build();
    }
}