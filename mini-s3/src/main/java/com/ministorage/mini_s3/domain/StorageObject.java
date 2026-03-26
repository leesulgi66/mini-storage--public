package com.ministorage.mini_s3.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class StorageObject {

    private String id;
    private String bucket;
    private String key;
    private String etag;
    private long size;
    private String contentType;
    private String acl;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public boolean isPublic() {
        return "public".equals(this.acl);
    }

    public String fullKey() {
        return this.bucket + "/" + this.key;
    }
}