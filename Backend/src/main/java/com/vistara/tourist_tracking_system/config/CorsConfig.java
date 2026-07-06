package com.vistara.tourist_tracking_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // ✅ Allowed origins
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:8087",
                "http://localhost:4200",
                "https://undrafted-erasable-crevice.ngrok-free.dev"
        ));

        // ✅ Allowed methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));

        // ✅ Allowed headers
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "X-Total-Count",
                "X-Page-Total"
        ));

        // ✅ Exposed headers
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Total-Count",
                "X-Page-Total"
        ));

        // ✅ Allow credentials
        config.setAllowCredentials(true);

        // ✅ Cache preflight for 1 hour
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}