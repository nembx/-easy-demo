package com.example.pay_demo.config;

import com.alipay.easysdk.kernel.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Lian
 */

@Configuration
public class PayConfig {

    @Bean
    public Config config(PayConfigInfo payConfigInfo){
        Config config = new Config();
        config.protocol = payConfigInfo.getProtocol();
        config.gatewayHost = payConfigInfo.getGatewayHost();
        config.signType = payConfigInfo.getSignType();
        config.appId = payConfigInfo.getAppId();
        config.merchantPrivateKey = payConfigInfo.getMerchantPrivateKey();
        config.alipayPublicKey = payConfigInfo.getAlipayPublicKey();
        config.notifyUrl = payConfigInfo.getNotifyUrl();
        return config;
    }

}
