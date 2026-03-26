package com.ministorage.admin_dashboard.controller;

import com.ministorage.admin_dashboard.dto.request.LoginRequest;
import com.ministorage.admin_dashboard.dto.request.RefreshRequest;
import com.ministorage.admin_dashboard.dto.response.LoginResponse;
import com.ministorage.admin_dashboard.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ── 로그인 ────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        String ip = httpRequest.getRemoteAddr();
        LoginResponse response = authService.login(request, ip);
        return ResponseEntity.ok(response);
    }

    // ── Access Token 재발급 ───────────────────────────────────
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody RefreshRequest request
    ) {
        LoginResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    // ── 로그아웃 ──────────────────────────────────────────────
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @Valid @RequestBody RefreshRequest request,
            @AuthenticationPrincipal String adminId,
            HttpServletRequest httpRequest
    ) {
        String ip = httpRequest.getRemoteAddr();
        authService.logout(request.getRefreshToken(), adminId, ip);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        return ResponseEntity.ok(authentication.getAuthorities());
    }
}