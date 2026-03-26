package com.ministorage.mini_s3.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Component
public class LocalStorageProvider implements StorageProvider {

    private final Path basePath;

    public LocalStorageProvider(
            @Value("${storage.local.base-path:./data/objects}") String basePath
    ) {
        this.basePath = Paths.get(basePath).toAbsolutePath();
        log.info("LocalStorageProvider initialized. basePath={}", this.basePath);
    }

    @Override
    public void store(String bucket, String key, InputStream data, long size, String contentType) {
        Path target = resolvePath(bucket, key);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(data, target, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Stored object: bucket={}, key={}, size={}", bucket, key, size);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store object: " + bucket + "/" + key, e);
        }
    }

    @Override
    public InputStream retrieve(String bucket, String key) {
        Path target = resolvePath(bucket, key);
        try {
            return Files.newInputStream(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to retrieve object: " + bucket + "/" + key, e);
        }
    }

    @Override
    public void delete(String bucket, String key) {
        Path target = resolvePath(bucket, key);
        try {
            Files.deleteIfExists(target);
            log.debug("Deleted object: bucket={}, key={}", bucket, key);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete object: " + bucket + "/" + key, e);
        }
    }

    @Override
    public boolean exists(String bucket, String key) {
        return Files.exists(resolvePath(bucket, key));
    }

    private Path resolvePath(String bucket, String key) {
        return basePath.resolve(bucket).resolve(key);
    }
}