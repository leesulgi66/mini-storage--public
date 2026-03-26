package com.ministorage.image_service.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class ImageVariant {

    private String id;              // ULID (var_01...)
    private String imageId;
    private String name;            // thumb_xs | thumb_sm | thumb_md | thumb_lg
    private String objectKey;
    private Integer width;
    private Integer height;
    private Long size;
    private String contentType;     // image/webp
    private String status;          // processing | ready | failed
    private OffsetDateTime createdAt;

    public boolean isReady() {
        return "ready".equals(this.status);
    }
}