package com.ministorage.admin_dashboard.service;

import com.ministorage.admin_dashboard.client.ImageServiceClient;
import com.ministorage.admin_dashboard.client.MiniS3Client;
import com.ministorage.admin_dashboard.dto.response.BucketResponse;
import com.ministorage.admin_dashboard.dto.response.DashboardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MiniS3Client miniS3Client;
    private final ImageServiceClient imageServiceClient;

    public DashboardResponse getDashboard() {
        List<String> bucketNames = miniS3Client.getBuckets();

        List<BucketResponse> buckets = bucketNames.stream()
                .map(name -> BucketResponse.builder()
                        .name(name)
                        .objectCount(miniS3Client.getObjectCount(name))
                        .totalSize(miniS3Client.getTotalSize(name))
                        .build())
                .toList();

        long totalImages = imageServiceClient.getTotalImages();
        long totalObjects = buckets.stream()
                .mapToLong(BucketResponse::getObjectCount)
                .sum();

        return DashboardResponse.builder()
                .totalImages(totalImages)
                .totalObjects(totalObjects)
                .buckets(buckets)
                .build();
    }
}