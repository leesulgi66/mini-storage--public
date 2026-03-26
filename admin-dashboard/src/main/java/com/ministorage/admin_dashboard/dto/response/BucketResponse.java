package com.ministorage.admin_dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BucketResponse {

    private String name;
    private long objectCount;
    private long totalSize;
}