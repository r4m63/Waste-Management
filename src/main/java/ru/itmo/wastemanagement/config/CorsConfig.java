package ru.itmo.wastemanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Configuration
public class CorsConfig {

    private final AppConfig appConfig;

    public CorsConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        List<String> allowedOrigins = new ArrayList<>();
        allowedOrigins.add(appConfig.getAdminFrontendURL());
        allowedOrigins.add(appConfig.getLandingFrontendURL());
        allowedOrigins.add(appConfig.getKioskFrontendURL());
        allowedOrigins.add(appConfig.getDriverFrontendURL());
        allowedOrigins.add(appConfig.getBackendURL());
        allowedOrigins.add("http://10.150.134.188:5175/"); //
        allowedOrigins.add("http://10.150.134.188:5174/"); //
        allowedOrigins.add("http://10.150.134.188:5173/"); //
        allowedOrigins = allowedOrigins.stream()
                .filter(Objects::nonNull)
                .toList();

        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

