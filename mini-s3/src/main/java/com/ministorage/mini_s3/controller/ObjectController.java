package com.ministorage.mini_s3.controller;

import com.ministorage.mini_s3.dto.request.SignRequest;
import com.ministorage.mini_s3.dto.response.ObjectListResponse;
import com.ministorage.mini_s3.dto.response.ObjectMetaResponse;
import com.ministorage.mini_s3.dto.response.SignedUrlResponse;
import com.ministorage.mini_s3.service.ObjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;

@RestController
@RequestMapping("/api/v1/objects")
@RequiredArgsConstructor
public class ObjectController {

    private final ObjectService objectService;

    // ── 업로드 ──────────────────────────────────────────────
    @PutMapping("/{bucket}/**")
    @ResponseStatus(HttpStatus.CREATED)
    public ObjectMetaResponse upload(
            @PathVariable String bucket,
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader(value = "X-Acl", defaultValue = "private") String acl,
            @RequestHeader(value = "X-Overwrite", defaultValue = "true") boolean overwrite,
            @RequestHeader(value = "Content-Length", required = false) Long contentLength,
            HttpServletRequest request
    ) throws Exception {
        String key = extractKey(request, "/api/v1/objects/" + bucket + "/");
        InputStream body = request.getInputStream();
        return objectService.upload(bucket, key, body, contentType, acl, overwrite, contentLength);
    }

    // ── 다운로드 ─────────────────────────────────────────────
    @GetMapping("/{bucket}/**")
    public void download(
            @PathVariable String bucket,
            @RequestParam(required = false) String token,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        if (token != null) {
            objectService.downloadByToken(token, response);
            return;
        }
        String key = extractKey(request, "/api/v1/objects/" + bucket + "/");
        objectService.download(bucket, key, null, response);
    }

    // ── HEAD ─────────────────────────────────────────────────
    @RequestMapping(value = "/{bucket}/**", method = RequestMethod.HEAD)
    public ResponseEntity<Void> head(
            @PathVariable String bucket,
            HttpServletRequest request
    ) {
        String key = extractKey(request, "/api/v1/objects/" + bucket + "/");
        return objectService.headObject(bucket, key);
    }

    // ── 삭제 ─────────────────────────────────────────────────
    @DeleteMapping("/{bucket}/**")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable String bucket,
            HttpServletRequest request
    ) {
        String key = extractKey(request, "/api/v1/objects/" + bucket + "/");
        objectService.delete(bucket, key);
    }

    // ── 목록 조회 ─────────────────────────────────────────────
    @GetMapping("/{bucket}")
    public ObjectListResponse list(
            @PathVariable String bucket,
            @RequestParam(required = false) String prefix,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) String cursor
    ) {
        return objectService.list(bucket, prefix, limit, cursor);
    }

    // ── Presigned URL 발급 ───────────────────────────────────
    @PostMapping("/{bucket}/{key}/sign")
    @ResponseStatus(HttpStatus.CREATED)
    public SignedUrlResponse sign(
            @PathVariable String bucket,
            @PathVariable String key,
            @Valid @RequestBody SignRequest request
    ) {
        return objectService.createSignedUrl(bucket, key, request);
    }

    // ── 헬퍼 ─────────────────────────────────────────────────
    private String extractKey(HttpServletRequest request, String prefix) {
        String uri = request.getRequestURI();
        int idx = uri.indexOf(prefix);
        return idx >= 0 ? uri.substring(idx + prefix.length()) : "";
    }
}