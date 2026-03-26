package com.ministorage.image_service.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class Image {

    private String id;              // ULID (img_01...)
    private String objectKey;
    private String bucket;
    private Integer width;
    private Integer height;
    private long size;
    private String contentType;
    private String acl;             // "public" | "private"
    private String etag;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public boolean isPublic() {
        return "public".equals(this.acl);
    }
}