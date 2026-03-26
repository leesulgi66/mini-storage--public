package com.ministorage.admin_dashboard.mapper;

import com.ministorage.admin_dashboard.domain.AuditLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AuditLogMapper {

    void insert(AuditLog auditLog);
    List<AuditLog> findRecent(int limit);
}