package com.ministorage.mini_s3.service;

import com.ministorage.mini_s3.domain.SignedUrl;
import com.ministorage.mini_s3.domain.StorageObject;
import com.ministorage.mini_s3.dto.request.SignRequest;
import com.ministorage.mini_s3.dto.response.ObjectListResponse;
import com.ministorage.mini_s3.dto.response.ObjectMetaResponse;
import com.ministorage.mini_s3.dto.response.SignedUrlResponse;
import com.ministorage.mini_s3.mapper.ObjectMapper;
import com.ministorage.mini_s3.mapper.SignedUrlMapper;
import com.ministorage.mini_s3.storage.StorageProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ObjectService {

    private final ObjectMapper objectMapper;
    private final SignedUrlMapper signedUrlMapper;
    private final StorageProvider storageProvider;

    @Value("${storage.max-file-size:52428800}")
    private long maxFileSize;

    @Value("${url.base:http://localhost:9000}")
    private String baseUrl;

    // ── 업로드 ──────────────────────────────────────────────

    @Transactional
    public ObjectMetaResponse upload(String bucket, String key, InputStream data,
                                     String contentType, String acl, boolean overwrite,
                                     Long contentLength) throws Exception {
        if (!overwrite && objectMapper.findByBucketAndKey(bucket, key).isPresent()) {
            throw new IllegalStateException("Object already exists: " + bucket + "/" + key);
        }

        if (contentLength != null && contentLength > maxFileSize) {
            throw new IllegalArgumentException("File too large. max=" + maxFileSize);
        }

        if (acl == null || acl.isBlank()) {
            acl = "private";
        }

        byte[] bytes = data.readAllBytes();
        String etag = calculateEtag(bytes);

        storageProvider.store(bucket, key,
                new java.io.ByteArrayInputStream(bytes), bytes.length, contentType);

        StorageObject object = StorageObject.builder()
                .bucket(bucket)
                .key(key)
                .etag(etag)
                .size(bytes.length)
                .contentType(contentType)
                .acl(acl)
                .build();

        objectMapper.insert(object);

        StorageObject saved = objectMapper.findByBucketAndKey(bucket, key)
                .orElseThrow(() -> new RuntimeException("Failed to save object"));

        log.info("Uploaded: bucket={}, key={}, size={}", bucket, key, bytes.length);
        return ObjectMetaResponse.from(saved);
    }

    // ── 다운로드 ─────────────────────────────────────────────

    public void download(String bucket, String key, String token,
                         HttpServletResponse response) throws IOException {
        StorageObject object = findObjectOrThrow(bucket, key);

        if (!object.isPublic()) {
            validateToken(token, bucket, key, "GET");
        }

        InputStream stream = storageProvider.retrieve(bucket, key);

        response.setContentType(object.getContentType());
        response.setHeader("ETag", object.getEtag());
        response.setHeader("Content-Length", String.valueOf(object.getSize()));

        if (key.contains("/variants/")) {
            response.setHeader("Cache-Control", "public, max-age=31536000, immutable");
        } else {
            response.setHeader("Cache-Control", "public, max-age=86400");
        }

        stream.transferTo(response.getOutputStream());
    }

    // ── HEAD ─────────────────────────────────────────────────

    public ResponseEntity<Void> headObject(String bucket, String key) {
        StorageObject object = findObjectOrThrow(bucket, key);

        return ResponseEntity.ok()
                .header("ETag", object.getEtag())
                .header("Content-Type", object.getContentType())
                .header("Content-Length", String.valueOf(object.getSize()))
                .header("X-Acl", object.getAcl())
                .build();
    }

    // ── 삭제 ─────────────────────────────────────────────────

    @Transactional
    public void delete(String bucket, String key) {
        storageProvider.delete(bucket, key);
        objectMapper.deleteByBucketAndKey(bucket, key);
        log.info("Deleted: bucket={}, key={}", bucket, key);
    }

    // ── 목록 조회 ─────────────────────────────────────────────

    public ObjectListResponse list(String bucket, String prefix, int limit, String cursor) {
        int safeLimit = Math.min(limit, 1000);

        List<StorageObject> objects = objectMapper.findByBucketWithPrefix(
                bucket, prefix, cursor, safeLimit + 1
        );

        String nextCursor = null;
        if (objects.size() > safeLimit) {
            objects = objects.subList(0, safeLimit);
            nextCursor = objects.get(objects.size() - 1).getKey();
        }

        List<ObjectMetaResponse> responses = objects.stream()
                .map(ObjectMetaResponse::from)
                .toList();

        return ObjectListResponse.of(responses, nextCursor);
    }

    // ── Presigned URL 발급 ───────────────────────────────────

    @Transactional
    public SignedUrlResponse createSignedUrl(String bucket, String key, SignRequest request) {
        if ("GET".equals(request.getMethod())) {
            findObjectOrThrow(bucket, key);
        }

        String token = generateToken(bucket, key);
        OffsetDateTime expiresAt = OffsetDateTime .now().plusSeconds(request.getTtl());

        SignedUrl signedUrl = SignedUrl.builder()
                .token(token)
                .bucket(bucket)
                .key(key)
                .method(request.getMethod())
                .expiresAt(expiresAt)
                .build();

        signedUrlMapper.insert(signedUrl);

        String url = baseUrl + "/api/v1/objects?token=" + token;

        return SignedUrlResponse.builder()
                .url(url)
                .method(request.getMethod())
                .expiresAt(expiresAt)
                .build();
    }

    // ── Presigned token 다운로드 ─────────────────────────────

    public void downloadByToken(String token, HttpServletResponse response) throws IOException {
        SignedUrl signedUrl = signedUrlMapper.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (signedUrl.isExpired()) {
            signedUrlMapper.deleteByToken(token);
            throw new IllegalArgumentException("Token expired");
        }

        download(signedUrl.getBucket(), signedUrl.getKey(), null, response);
    }

    // ── 헬퍼 ─────────────────────────────────────────────────

    private StorageObject findObjectOrThrow(String bucket, String key) {
        return objectMapper.findByBucketAndKey(bucket, key)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Object not found: " + bucket + "/" + key));
    }

    private void validateToken(String token, String bucket, String key, String method) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token required for private object");
        }

        SignedUrl signedUrl = signedUrlMapper.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (signedUrl.isExpired()) {
            throw new IllegalArgumentException("Token expired");
        }
        if (!signedUrl.getBucket().equals(bucket) || !signedUrl.getKey().equals(key)) {
            throw new IllegalArgumentException("Token mismatch");
        }
        if (!signedUrl.getMethod().equals(method)) {
            throw new IllegalArgumentException("Token method mismatch");
        }
    }

    private String calculateEtag(byte[] bytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(bytes);
        return HexFormat.of().formatHex(hash);
    }

    private String generateToken(String bucket, String key) {
        String raw = bucket + "/" + key + "/" + UUID.randomUUID();
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes());
    }
}