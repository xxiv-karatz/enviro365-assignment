package com.enviro.assessment.junior.khulekani.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Allows the static frontend (served from a different origin/port than the Spring Boot
 * API) to call the REST endpoints from the browser. Without this, the browser blocks the
 * fetch() calls in frontend/js/api.js with a CORS error before they ever reach the backend.
 *
 * allowedOriginPatterns("*") is intentionally permissive here because the frontend is a
 * plain HTML/JS app with no fixed origin during local development/marking. In a real
 * deployment this should be narrowed to the frontend's actual origin(s).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
