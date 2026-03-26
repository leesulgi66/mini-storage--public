package com.ministorage.admin_dashboard.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class ImageServiceClient {

    private final WebClient webClient;

    public ImageServiceClient(
            @Value("${image-service.base-url}") String baseUrl,
            @Value("${image-service.api-key}") String apiKey
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-API-Key", apiKey)
                .build();
    }

    // ── 이미지 총 개수 조회 ───────────────────────────────────

    public long getTotalImages() {
        try {
            JsonNode response = webClient.get()
                    .uri("/api/v1/images")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("count")) {
                return response.get("count").asLong();
            }
            return 0;
        } catch (Exception e) {
            log.error("Failed to get total images", e);
            return 0;
        }
    }

    // ── 이미지 목록 조회 ──────────────────────────────────────

    public JsonNode getImages(int limit, String cursor) {
        try {
            String uri = "/api/v1/images?limit=" + limit;
            if (cursor != null) uri += "&cursor=" + cursor;

            return webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to get images", e);
            return null;
        }
    }

    // ── 이미지 삭제 ───────────────────────────────────────────

    public void deleteImage(String id) {
        webClient.delete()
                .uri("/api/v1/images/" + id)
                .retrieve()
                .toBodilessEntity()
                .doOnError(e -> log.error("Failed to delete image: {}", id, e))
                .block();
    }

    // ── 이미지 업로드 ──────────────────────────────────────────

    public JsonNode uploadImage(MultipartFile file) {
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", file.getResource())
                    .filename(file.getOriginalFilename())
                    .contentType(MediaType.parseMediaType(file.getContentType()));

            return webClient.post()
                    .uri("/api/v1/images")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to upload image", e);
            return null;
        }
    }
}