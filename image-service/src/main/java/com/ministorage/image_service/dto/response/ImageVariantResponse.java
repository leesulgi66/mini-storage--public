package com.ministorage.image_service.dto.response;

import com.ministorage.image_service.domain.ImageVariant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageVariantResponse {

    private String id;
    private String name;
    private String objectKey;
    private Integer width;
    private Integer height;
    private Long size;
    private String contentType;
    private String status;

    public static ImageVariantResponse from(ImageVariant variant) {
        return ImageVariantResponse.builder()
                .id(variant.getId())
                .name(variant.getName())
                .objectKey(variant.getObjectKey())
                .width(variant.getWidth())
                .height(variant.getHeight())
                .size(variant.getSize())
                .contentType(variant.getContentType())
                .status(variant.getStatus())
                .build();
    }
}