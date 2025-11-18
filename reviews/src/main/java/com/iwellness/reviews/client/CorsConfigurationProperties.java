package com.iwellness.reviews.client;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * CORS Configuration Properties
 */
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsConfigurationProperties {
    
    private List<String> allowedOrigins;
    
    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }
    
    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
}
