package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OpenRouterConfig {
    
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;
    
    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;
    
    @Value("${spring.ai.openai.chat.options.model}")
    private String model;
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public OpenRouterProperties openRouterProperties() {
        return new OpenRouterProperties(apiKey, baseUrl, model);
    }
    
    public static class OpenRouterProperties {
        private final String apiKey;
        private final String baseUrl;
        private final String model;
        
        public OpenRouterProperties(String apiKey, String baseUrl, String model) {
            this.apiKey = apiKey;
            this.baseUrl = baseUrl;
            this.model = model;
        }
        
        public String getApiKey() {
            return apiKey;
        }
        
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public String getModel() {
            return model;
        }
    }
}