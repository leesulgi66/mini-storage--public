package com.ministorage.mini_s3.mapper;

import com.ministorage.mini_s3.domain.SignedUrl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface SignedUrlMapper {

    void insert(SignedUrl signedUrl);

    Optional<SignedUrl> findByToken(@Param("token") String token);

    void deleteByToken(@Param("token") String token);

    void deleteExpired();
}