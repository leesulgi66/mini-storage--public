package com.ministorage.admin_dashboard.mapper;

import com.ministorage.admin_dashboard.domain.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface AdminUserMapper {

    Optional<AdminUser> findByEmail(@Param("email") String email);

    Optional<AdminUser> findById(@Param("id") String id);
}