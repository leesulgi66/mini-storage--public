package com.ministorage.image_service.controller;

import com.ministorage.image_service.dto.response.ImageListResponse;
import com.ministorage.image_service.dto.response.ImageResponse;
import com.ministorage.image_service.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    // ── 업로드 ──────────────────────────────────────────────
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImageResponse upload(
            @RequestPart("file") MultipartFile file,
            @RequestHeader(value = "X-Acl", defaultValue = "public") String acl
    ) throws Exception {
        return imageService.upload(file, acl);
    }

    // ── 단건 조회 ─────────────────────────────────────────────
    @GetMapping("/{id}")
    public ImageResponse getById(@PathVariable String id) {
        return imageService.getById(id);
    }

    // ── 목록 조회 ─────────────────────────────────────────────
    @GetMapping
    public ImageListResponse getList(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String cursor
    ) {
        return imageService.getList(limit, cursor);
    }

    // ── 삭제 ─────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        imageService.delete(id);
    }
}