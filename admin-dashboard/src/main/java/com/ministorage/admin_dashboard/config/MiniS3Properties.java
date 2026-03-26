package com.ministorage.admin_dashboard.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "mini-s3")
public class MiniS3Properties {

    private String baseUrl;
    private String apiKey;
    private List<String> buckets;
}