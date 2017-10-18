package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoConfiguration {
    @Bean
    EthereumService ethereumService(@Value("${config.nodeUrl}") String senderAccountPassword, @Value("${config.senderAccountPassword}") String nodeUrl) {
        return new EthereumService(senderAccountPassword, nodeUrl);
    }
}
