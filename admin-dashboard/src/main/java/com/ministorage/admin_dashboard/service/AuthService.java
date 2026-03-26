package com.ministorage.admin_dashboard.service;

import com.ministorage.admin_dashboard.domain.AdminUser;
import com.ministorage.admin_dashboard.domain.AuditLog;
import com.ministorage.admin_dashboard.domain.RefreshToken;
import com.ministorage.admin_dashboard.dto.request.LoginRequest;
import com.ministorage.admin_dashboard.dto.response.LoginResponse;
import com.ministorage.admin_dashboard.mapper.AdminUserMapper;
import com.ministorage.admin_dashboard.mapper.AuditLogMapper;
import com.ministorage.admin_dashboard.mapper.RefreshTokenMapper;
import com.ministorage.admin_dashboard.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminUserMapper adminUserMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final AuditLogMapper auditLogMapper;
    private final JwtProvider jwtProvider;
    private final BCryptPasswordEncoder passwordEncoder;

    // ── 로그인 ────────────────────────────────────────────────

    @Transactional
    public LoginResponse login(LoginRequest request, String ip) {
        AdminUser user = adminUserMapper.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        // Refresh Token DB 저장 (hash로)
        String tokenHash = hashToken(refreshToken);
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .adminId(user.getId())
                .tokenHash(tokenHash)
                .expiresAt(OffsetDateTime.now().plusSeconds(jwtProvider.getRefreshTokenExpiry()))
                .build();

        refreshTokenMapper.insert(refreshTokenEntity);

        // Audit 로그
        auditLogMapper.insert(AuditLog.builder()
                .adminId(user.getId())
                .action("auth.login")
                .target(user.getEmail())
                .ip(ip)
                .build());

        log.info("Login: email={}, ip={}", user.getEmail(), ip);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProvider.getAccessTokenExpiry())
                .role(user.getRole())
                .build();
    }

    // ── Access Token 재발급 ───────────────────────────────────

    @Transactional
    public LoginResponse refresh(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다");
        }

        String tokenHash = hashToken(refreshToken);
        RefreshToken stored = refreshTokenMapper.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Refresh Token을 찾을 수 없습니다"));

        if (stored.isExpired()) {
            refreshTokenMapper.deleteByTokenHash(tokenHash);
            throw new IllegalArgumentException("Refresh Token이 만료되었습니다");
        }

        AdminUser user = adminUserMapper.findById(stored.getAdminId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        String newAccessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProvider.getAccessTokenExpiry())
                .role(user.getRole())
                .build();
    }

    // ── 로그아웃 ──────────────────────────────────────────────

    @Transactional
    public void logout(String refreshToken, String adminId, String ip) {
        String tokenHash = hashToken(refreshToken);
        refreshTokenMapper.deleteByTokenHash(tokenHash);

        auditLogMapper.insert(AuditLog.builder()
                .adminId(adminId)
                .action("auth.logout")
                .ip(ip)
                .build());

        log.info("Logout: adminId={}, ip={}", adminId, ip);
    }

    // ── 헬퍼 ─────────────────────────────────────────────────

    private String hashToken(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Token hashing failed", e);
        }
    }
}