package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoConfiguration {
    @Bean
    EthereumService ethereumService(@Value("${config.ethereumUrl}") String nodeUrl, @Value("${config.ethereumSenderAccountPassword}") String senderAccountPassword) {
        return new EthereumService(nodeUrl, senderAccountPassword);
    }

    @Bean
    QuorumService quorumService(@Value("${config.quorumUrl}") String nodeUrl) {
        return new QuorumService(nodeUrl);
    }
}
