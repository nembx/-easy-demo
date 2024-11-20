package com.example.pay_demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Lian
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "alipay.easy")
public class PayConfigInfo {

    private String protocol;

    private String gatewayHost;

    private String signType;

    private String appId;

    private String merchantPrivateKey;

    private String alipayPublicKey;

    private String notifyUrl;
}
