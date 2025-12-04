package ru.itmo.wastemanagement.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
@Getter
public class AppConfig {

    @Value("${app.urls.admin-frontend}")
    private String adminFrontendURL;

    @Value("${app.urls.landing-frontend}")
    private String landingFrontendURL;

    @Value("${app.urls.kiosk-frontend}")
    private String kioskFrontendURL;

    @Value("${app.urls.driver-frontend}")
    private String driverFrontendURL;

    @Value("${app.urls.backend}")
    private String backendURL;

}
