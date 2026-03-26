package com.ministorage.admin_dashboard.mapper;

import com.ministorage.admin_dashboard.domain.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface RefreshTokenMapper {

    void insert(RefreshToken refreshToken);

    Optional<RefreshToken> findByTokenHash(@Param("tokenHash") String tokenHash);

    void deleteByTokenHash(@Param("tokenHash") String tokenHash);

    void deleteByAdminId(@Param("adminId") String adminId);

    void deleteExpired();
}