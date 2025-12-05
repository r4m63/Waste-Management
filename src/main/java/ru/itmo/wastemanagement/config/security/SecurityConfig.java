package ru.itmo.wastemanagement.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security Configuration.
 * 
 * ВАЖНО: Для упрощения разработки все эндпоинты открыты.
 * В production необходимо настроить аутентификацию и авторизацию.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Публичные эндпоинты (для landing и kiosk)
                        .requestMatchers(
                                "/api/garbage-points/**",
                                "/api/fractions/**",
                                "/api/container-sizes/**",
                                "/api/kiosk-orders/**"
                        ).permitAll()

                        // Эндпоинты для входа/регистрации (будут добавлены)
                        .requestMatchers("/api/auth/**").permitAll()

                        // TODO: Настроить после реализации аутентификации
                        // .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // .requestMatchers("/api/driver/**").hasAnyRole("COURIER", "WORKER")

                        // Временно разрешаем все для разработки
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
