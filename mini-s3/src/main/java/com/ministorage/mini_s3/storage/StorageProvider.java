package com.ministorage.mini_s3.storage;

import java.io.InputStream;

/**
 * 저장 백엔드 추상화 인터페이스.
 * LocalStorageProvider / S3StorageProvider 로 교체 가능.
 */
public interface StorageProvider {

    void store(String bucket, String key, InputStream data, long size, String contentType);

    InputStream retrieve(String bucket, String key);

    void delete(String bucket, String key);

    boolean exists(String bucket, String key);
}