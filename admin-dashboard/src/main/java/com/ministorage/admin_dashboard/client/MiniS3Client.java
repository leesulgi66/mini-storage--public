package com.ministorage.admin_dashboard.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.ministorage.admin_dashboard.config.MiniS3Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Component
public class MiniS3Client {

    private final WebClient webClient;
    private final MiniS3Properties properties;

    public MiniS3Client(MiniS3Properties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("X-API-Key", properties.getApiKey())
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    public List<String> getBuckets() {
        return properties.getBuckets();
    }

    public long getObjectCount(String bucket) {
        try {
            JsonNode response = webClient.get()
                    .uri("/api/v1/objects/" + bucket)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("count")) {
                return response.get("count").asLong();
            }
            return 0;
        } catch (Exception e) {
            log.error("Failed to get object count for bucket: {}", bucket, e);
            return 0;
        }
    }

    public byte[] downloadObject(String bucket, String key) {
        try {
            return webClient.get()
                    .uri("/api/v1/objects/" + bucket + "/" + key)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to download object: {}/{}", bucket, key, e);
            return null;
        }
    }

    public String getContentType(String bucket, String key) {
        try {
            return webClient.head()
                    .uri("/api/v1/objects/" + bucket + "/" + key)
                    .retrieve()
                    .toBodilessEntity()
                    .map(res -> {
                        var ct = res.getHeaders().getContentType();
                        return ct != null ? ct.toString() : "application/octet-stream";
                    })
                    .block();
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }

    public JsonNode listObjects(String bucket, String prefix, int limit) {
        try {
            String uri = "/api/v1/objects/" + bucket + "?limit=" + limit;
            if (prefix != null) uri += "&prefix=" + prefix;
            return webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to list objects: {}", bucket, e);
            return null;
        }
    }

    public long getTotalSize(String bucket) {
        try {
            JsonNode response = webClient.get()
                    .uri("/api/v1/objects/" + bucket)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("objects")) {
                long total = 0;
                for (JsonNode obj : response.get("objects")) {
                    total += obj.get("size").asLong(0);
                }
                return total;
            }
            return 0;
        } catch (Exception e) {
            log.error("Failed to get total size for bucket: {}", bucket, e);
            return 0;
        }
    }
}