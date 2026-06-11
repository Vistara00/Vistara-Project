package com.vistara.tourist_tracking_system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mpesa")
public class MpesaConfig {
    private String baseUrl;
    private String consumerKey;
    private String consumerSecret;
    private String shortCode;
    private String passKey;
    private String stkCallbackUrl;
    private String initiatorName;
    private String securityCredential;
    private String b2cResultUrl;
    private String b2cTimeoutUrl;
}