package com.ministorage.image_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class MiniS3Client {

    private final WebClient webClient;
    private final String apiKey;

    public MiniS3Client(
            @Value("${mini-s3.base-url}") String baseUrl,
            @Value("${mini-s3.api-key}") String apiKey
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-API-Key", apiKey)
                .build();
        this.apiKey = apiKey;
    }

    // ── 객체 업로드 ──────────────────────────────────────────

    public void upload(String bucket, String key, byte[] data,
                       String contentType, String acl) {
        webClient.put()
                .uri("/api/v1/objects/" + bucket + "/" + sanitizeKey(key))
                .contentType(MediaType.parseMediaType(contentType))
                .header("X-Acl", acl)
                .bodyValue(new ByteArrayResource(data))
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(res -> log.debug("Uploaded to mini-s3: {}/{}", bucket, key))
                .doOnError(e -> log.error("Failed to upload to mini-s3: {}/{}", bucket, key, e))
                .block();
    }

    // ── 객체 다운로드 ─────────────────────────────────────────

    public byte[] download(String bucket, String key) {
        return webClient.get()
                .uri("/api/v1/objects/" + bucket + "/" + sanitizeKey(key))
                .retrieve()
                .bodyToMono(byte[].class)
                .doOnError(e -> log.error("Failed to download from mini-s3: {}/{}", bucket, key, e))
                .block();
    }

    // ── 객체 삭제 ─────────────────────────────────────────────

    public void delete(String bucket, String key) {
        webClient.delete()
                .uri("/api/v1/objects/" + bucket + "/" + sanitizeKey(key))
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(res -> log.debug("Deleted from mini-s3: {}/{}", bucket, key))
                .doOnError(e -> log.error("Failed to delete from mini-s3: {}/{}", bucket, key, e))
                .block();
    }

    private String sanitizeKey(String key) {
        return key.replaceAll("\\s+", "_");
    }
}