package com.ministorage.mini_s3.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class SignRequest {

    @NotBlank(message = "method는 필수입니다")
    @Pattern(regexp = "GET|PUT", message = "method는 GET 또는 PUT만 허용됩니다")
    private String method;

    @Min(value = 60, message = "ttl은 최소 60초입니다")
    @Max(value = 604800, message = "ttl은 최대 7일(604800초)입니다")
    private int ttl = 3600;        // 기본값 1시간
}