package org.artbok.translator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")  // Allow all origins
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // Allow all common HTTP methods
                .allowedHeaders("*")  // Allow all headers
                .allowCredentials(false)  // Disable credentials (must be false when allowing all origins)
                .maxAge(3600);  // Preflight request cache for 1 hour
    }
}
