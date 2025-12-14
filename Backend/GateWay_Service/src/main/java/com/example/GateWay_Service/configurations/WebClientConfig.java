package com.example.GateWay_Service.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${pdf.service.base-url}")
    private String pdfServiceBaseUrl;

    @Value("${image.service.base-url}")
    private String imageServiceBaseUrl;

    @Bean
    public WebClient pdfWebClient(){

        ExchangeStrategies strategies = ExchangeStrategies.builder() // bump maxInMemorySize so big Pdfs don't blow up with databufferlimit shouts
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(15 * 1024 * 1024)
                )
                .build();

        return WebClient.builder()
                .baseUrl(pdfServiceBaseUrl)
                .exchangeStrategies(strategies)
                .build();
    }

    @Bean
    public WebClient imageWebClient(){

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs( configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(15 * 1024 * 1024)
                )
                .build();

        return WebClient.builder()
                .baseUrl(imageServiceBaseUrl)
                .exchangeStrategies(strategies)
                .build();
    }
}
