package com.ministorage.admin_dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardResponse {

    private long totalImages;
    private long totalObjects;
    private List<BucketResponse> buckets;
}