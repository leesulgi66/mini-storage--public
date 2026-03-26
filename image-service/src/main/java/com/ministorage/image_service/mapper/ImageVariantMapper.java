package com.ministorage.image_service.mapper;

import com.ministorage.image_service.domain.ImageVariant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ImageVariantMapper {

    void insert(ImageVariant variant);

    List<ImageVariant> findByImageId(@Param("imageId") String imageId);

    void updateStatus(
            @Param("id") String id,
            @Param("status") String status
    );

    void deleteByImageId(@Param("imageId") String imageId);
}