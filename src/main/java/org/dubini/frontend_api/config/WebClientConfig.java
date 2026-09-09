package org.dubini.frontend_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @Scope("prototype")
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
