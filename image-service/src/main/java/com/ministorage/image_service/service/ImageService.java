package com.ministorage.image_service.service;

import com.ministorage.image_service.client.MiniS3Client;
import com.ministorage.image_service.domain.Image;
import com.ministorage.image_service.domain.ImageVariant;
import com.ministorage.image_service.dto.response.ImageListResponse;
import com.ministorage.image_service.dto.response.ImageResponse;
import com.ministorage.image_service.dto.response.ImageVariantResponse;
import com.ministorage.image_service.mapper.ImageMapper;
import com.ministorage.image_service.mapper.ImageVariantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageMapper imageMapper;
    private final ImageVariantMapper imageVariantMapper;
    private final MiniS3Client miniS3Client;

    @Value("${mini-s3.bucket}")
    private String defaultBucket;

    @Value("${image.max-size:52428800}")
    private long maxSize;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    // 썸네일 변환 사이즈
    private static final Map<String, Integer> VARIANT_SIZES = Map.of(
            "thumb_xs", 120,
            "thumb_sm", 240,
            "thumb_md", 480,
            "thumb_lg", 1280
    );

    // ── 업로드 ──────────────────────────────────────────────

    @Transactional
    public ImageResponse upload(MultipartFile file, String acl) throws Exception {
        // MIME 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + contentType);
        }

        // 파일 크기 검증
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기 초과: max=" + maxSize);
        }

        byte[] bytes = file.getBytes();

        // ETag 계산
        String etag = calculateEtag(bytes);

        // 이미지 메타 읽기 (width, height)
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
        Integer width = bufferedImage != null ? bufferedImage.getWidth() : null;
        Integer height = bufferedImage != null ? bufferedImage.getHeight() : null;

        // ACL 기본값
        if (acl == null || acl.isBlank()) acl = "public";

        // ULID 생성 (간단 버전)
        String imageId = generateId("img");
        String objectKey = imageId + "/" + file.getOriginalFilename();

        // mini S3에 업로드
        miniS3Client.upload(defaultBucket, objectKey, bytes, contentType, acl);

        // DB 저장
        Image image = Image.builder()
                .id(imageId)
                .objectKey(objectKey)
                .bucket(defaultBucket)
                .width(width)
                .height(height)
                .size(file.getSize())
                .contentType(contentType)
                .acl(acl)
                .etag(etag)
                .build();

        imageMapper.insert(image);

        // 썸네일 생성 (비동기 처리 - 여기선 동기로 구현)
        if (bufferedImage != null && !contentType.equals("image/gif")) {
            generateVariants(imageId, objectKey, bufferedImage, acl);
        }

        Image saved = imageMapper.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Failed to save image"));

        List<ImageVariant> variants = imageVariantMapper.findByImageId(imageId);
        List<ImageVariantResponse> variantResponses = variants.stream()
                .map(ImageVariantResponse::from)
                .toList();

        ImageResponse response = ImageResponse.from(saved);
        return ImageResponse.builder()
                .id(response.getId())
                .objectKey(response.getObjectKey())
                .bucket(response.getBucket())
                .width(response.getWidth())
                .height(response.getHeight())
                .size(response.getSize())
                .contentType(response.getContentType())
                .acl(response.getAcl())
                .etag(response.getEtag())
                .createdAt(response.getCreatedAt())
                .variants(variantResponses)
                .build();
    }

    // ── 단건 조회 ─────────────────────────────────────────────

    public ImageResponse getById(String id) {
        Image image = imageMapper.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + id));

        List<ImageVariant> variants = imageVariantMapper.findByImageId(id);
        List<ImageVariantResponse> variantResponses = variants.stream()
                .map(ImageVariantResponse::from)
                .toList();

        return ImageResponse.builder()
                .id(image.getId())
                .objectKey(image.getObjectKey())
                .bucket(image.getBucket())
                .width(image.getWidth())
                .height(image.getHeight())
                .size(image.getSize())
                .contentType(image.getContentType())
                .acl(image.getAcl())
                .etag(image.getEtag())
                .createdAt(image.getCreatedAt())
                .variants(variantResponses)
                .build();
    }

    // ── 목록 조회 ─────────────────────────────────────────────

    public ImageListResponse getList(int limit, String cursor) {
        int safeLimit = Math.min(limit, 100);

        List<Image> images = imageMapper.findAll(cursor, safeLimit + 1);

        String nextCursor = null;
        if (images.size() > safeLimit) {
            images = images.subList(0, safeLimit);
            nextCursor = images.get(images.size() - 1).getId();
        }

        List<ImageResponse> responses = images.stream()
                .map(ImageResponse::from)
                .toList();

        return ImageListResponse.of(responses, nextCursor);
    }

    // ── 삭제 ─────────────────────────────────────────────────

    @Transactional
    public void delete(String id) {
        Image image = imageMapper.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + id));

        // 변환본 삭제
        List<ImageVariant> variants = imageVariantMapper.findByImageId(id);
        for (ImageVariant variant : variants) {
            miniS3Client.delete(image.getBucket(), variant.getObjectKey());
        }

        // 원본 삭제
        miniS3Client.delete(image.getBucket(), image.getObjectKey());

        // DB 삭제 (CASCADE로 variants도 삭제)
        imageMapper.deleteById(id);

        log.info("Deleted image: id={}", id);
    }

    // ── 썸네일 생성 ───────────────────────────────────────────

    private void generateVariants(String imageId, String originalKey,
                                  BufferedImage original, String acl) {
        VARIANT_SIZES.forEach((name, size) -> {
            try {
                // 리사이즈
                BufferedImage resized = resizeImage(original, size);

                // PNG로 변환 (WebP 미지원 시 대체)
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(resized, "png", baos);
                byte[] variantBytes = baos.toByteArray();

                String variantId = generateId("var");
                String variantKey = imageId + "/variants/" + name + ".png";

                // mini S3 업로드
                miniS3Client.upload(defaultBucket, variantKey, variantBytes, "image/png", acl);

                // DB 저장
                ImageVariant variant = ImageVariant.builder()
                        .id(variantId)
                        .imageId(imageId)
                        .name(name)
                        .objectKey(variantKey)
                        .width(resized.getWidth())
                        .height(resized.getHeight())
                        .size((long) variantBytes.length)
                        .contentType("image/png")
                        .status("ready")
                        .build();

                imageVariantMapper.insert(variant);
                log.debug("Generated variant: imageId={}, name={}", imageId, name);

            } catch (Exception e) {
                log.error("Failed to generate variant: imageId={}, name={}", imageId, name, e);
            }
        });
    }

    private BufferedImage resizeImage(BufferedImage original, int maxDimension) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        // 비율 유지 리사이즈
        double ratio = Math.min(
                (double) maxDimension / originalWidth,
                (double) maxDimension / originalHeight
        );

        // 이미 작으면 리사이즈 불필요
        if (ratio >= 1.0) return original;

        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        java.awt.image.BufferedImage resized =
                new java.awt.image.BufferedImage(newWidth, newHeight,
                        BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return resized;
    }

    // ── 헬퍼 ─────────────────────────────────────────────────

    private String calculateEtag(byte[] bytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        return HexFormat.of().formatHex(md.digest(bytes));
    }

    private String generateId(String prefix) {
        return prefix + "_" + Instant.now().toEpochMilli() +
                String.format("%06d", (int)(Math.random() * 1000000));
    }
}