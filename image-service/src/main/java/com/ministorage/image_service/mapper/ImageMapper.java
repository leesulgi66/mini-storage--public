package com.ministorage.image_service.mapper;

import com.ministorage.image_service.domain.Image;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ImageMapper {

    void insert(Image image);

    Optional<Image> findById(@Param("id") String id);

    Optional<Image> findByObjectKey(@Param("objectKey") String objectKey);

    List<Image> findAll(
            @Param("cursor") String cursor,
            @Param("limit") int limit
    );

    long count();

    void deleteById(@Param("id") String id);
}