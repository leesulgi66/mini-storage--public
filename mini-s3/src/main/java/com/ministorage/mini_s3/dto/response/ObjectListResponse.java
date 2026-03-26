package com.ministorage.mini_s3.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ObjectListResponse {

    private List<ObjectMetaResponse> objects;
    private int count;
    private String nextCursor;     // 다음 페이지 커서 (없으면 null)
    private boolean hasMore;

    public static ObjectListResponse of(List<ObjectMetaResponse> objects, String nextCursor) {
        return ObjectListResponse.builder()
                .objects(objects)
                .count(objects.size())
                .nextCursor(nextCursor)
                .hasMore(nextCursor != null)
                .build();
    }
}