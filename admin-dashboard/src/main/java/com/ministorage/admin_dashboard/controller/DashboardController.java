package com.ministorage.admin_dashboard.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.ministorage.admin_dashboard.client.ImageServiceClient;
import com.ministorage.admin_dashboard.client.MiniS3Client;
import com.ministorage.admin_dashboard.domain.AuditLog;
import com.ministorage.admin_dashboard.dto.response.DashboardResponse;
import com.ministorage.admin_dashboard.mapper.AuditLogMapper;
import com.ministorage.admin_dashboard.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final ImageServiceClient imageServiceClient;
    private final MiniS3Client miniS3Client;
    private final AuditLogMapper auditLogMapper;

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard() {
        return dashboardService.getDashboard();
    }

    @GetMapping("/images")
    public ResponseEntity<JsonNode> getImages(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String cursor) {
        JsonNode result = imageServiceClient.getImages(limit, cursor);
        if (result == null) return ResponseEntity.internalServerError().build();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/images/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable String id) {
        imageServiceClient.deleteImage(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/proxy/**")
    public ResponseEntity<byte[]> proxyObject(HttpServletRequest request) {

        String uri = request.getRequestURI();
        String afterProxy = uri.substring(uri.indexOf("/api/v1/proxy/") + "/api/v1/proxy/".length());
        int slashIdx = afterProxy.indexOf('/');
        if (slashIdx < 0) return ResponseEntity.notFound().build();

        String bucket = afterProxy.substring(0, slashIdx);
        String key = afterProxy.substring(slashIdx + 1);

        byte[] data = miniS3Client.downloadObject(bucket, key);
        if (data == null) return ResponseEntity.notFound().build();

        String contentType = miniS3Client.getContentType(bucket, key);
        return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .header("Cache-Control", "max-age=3600")
                .body(data);
    }

    @PostMapping(value = "/images", consumes = "multipart/form-data")
    public ResponseEntity<JsonNode> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            JsonNode result = imageServiceClient.uploadImage(file);
            if (result == null) return ResponseEntity.internalServerError().build();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to upload image", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/buckets")
    public ResponseEntity<JsonNode> getBuckets() {
        try {
            List<String> buckets = miniS3Client.getBuckets();
            return ResponseEntity.ok(
                    new com.fasterxml.jackson.databind.node.ArrayNode(
                            com.fasterxml.jackson.databind.node.JsonNodeFactory.instance
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/buckets/{bucket}/objects")
    public ResponseEntity<JsonNode> getBucketObjects(
            @PathVariable String bucket,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) String prefix) {
        JsonNode result = miniS3Client.listObjects(bucket, prefix, limit);
        if (result == null) return ResponseEntity.internalServerError().build();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "20") int limit) {
        List<AuditLog> logs = auditLogMapper.findRecent(limit);
        return ResponseEntity.ok(logs);
    }
}