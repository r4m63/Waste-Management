package ru.itmo.wastemanagement.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.filter.CorsFilter;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    @Test
    void corsFilterContainsConfiguredOriginsAndMethods() {
        AppConfig appConfig = new AppConfig();
        ReflectionTestUtils.setField(appConfig, "adminFrontendURL", "http://admin");
        ReflectionTestUtils.setField(appConfig, "landingFrontendURL", "http://landing");
        ReflectionTestUtils.setField(appConfig, "kioskFrontendURL", "http://kiosk");
        ReflectionTestUtils.setField(appConfig, "driverFrontendURL", "http://driver");
        ReflectionTestUtils.setField(appConfig, "backendURL", "http://backend");

        CorsConfig corsConfig = new CorsConfig(appConfig);
        CorsFilter filter = corsConfig.corsFilter();

        assertThat(filter).isNotNull();
        assertThat(appConfig.getAdminFrontendURL()).isEqualTo("http://admin");
        assertThat(appConfig.getLandingFrontendURL()).isEqualTo("http://landing");
        assertThat(appConfig.getKioskFrontendURL()).isEqualTo("http://kiosk");
        assertThat(appConfig.getDriverFrontendURL()).isEqualTo("http://driver");
        assertThat(appConfig.getBackendURL()).isEqualTo("http://backend");
    }
}
