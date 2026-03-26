package com.ministorage.mini_s3.mapper;

import com.ministorage.mini_s3.domain.StorageObject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ObjectMapper {

    void insert(StorageObject object);

    Optional<StorageObject> findByBucketAndKey(
            @Param("bucket") String bucket,
            @Param("key") String key
    );

    List<StorageObject> findByBucketWithPrefix(
            @Param("bucket") String bucket,
            @Param("prefix") String prefix,
            @Param("cursor") String cursor,
            @Param("limit") int limit
    );

    long countByBucket(@Param("bucket") String bucket);

    long sumSizeByBucket(@Param("bucket") String bucket);

    void deleteByBucketAndKey(
            @Param("bucket") String bucket,
            @Param("key") String key
    );

    List<String> findDistinctBuckets();
}