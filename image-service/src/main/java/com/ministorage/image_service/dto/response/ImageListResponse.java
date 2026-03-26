package com.ministorage.image_service.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ImageListResponse {

    private List<ImageResponse> images;
    private int count;
    private String nextCursor;
    private boolean hasMore;

    public static ImageListResponse of(List<ImageResponse> images, String nextCursor) {
        return ImageListResponse.builder()
                .images(images)
                .count(images.size())
                .nextCursor(nextCursor)
                .hasMore(nextCursor != null)
                .build();
    }
}