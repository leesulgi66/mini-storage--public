package com.ministorage.image_service.dto.response;

import com.ministorage.image_service.domain.Image;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class ImageResponse {

    private String id;
    private String objectKey;
    private String bucket;
    private Integer width;
    private Integer height;
    private long size;
    private String contentType;
    private String acl;
    private String etag;
    private OffsetDateTime createdAt;
    private List<ImageVariantResponse> variants;

    public static ImageResponse from(Image image) {
        return ImageResponse.builder()
                .id(image.getId())
                .objectKey(image.getObjectKey())
                .bucket(image.getBucket())
                .width(image.getWidth())
                .height(image.getHeight())
                .size(image.getSize())
                .contentType(image.getContentType())
                .acl(image.getAcl())
                .etag(image.getEtag())
                .createdAt(image.getCreatedAt())
                .build();
    }
}