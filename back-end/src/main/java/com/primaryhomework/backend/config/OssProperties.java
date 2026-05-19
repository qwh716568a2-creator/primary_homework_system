package com.primaryhomework.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Data
@Component
@ConfigurationProperties(prefix = "app.oss")
public class OssProperties {

    private boolean enabled;
    private String endpoint;
    private String region;
    private String bucketName;
    private String accessKeyId;
    private String accessKeySecret;
    private String publicBaseUrl;
    private String prefix = "primary-homework";

    public boolean isConfigured() {
        return enabled
                && StringUtils.hasText(endpoint)
                && StringUtils.hasText(bucketName)
                && StringUtils.hasText(accessKeyId)
                && StringUtils.hasText(accessKeySecret);
    }
}
