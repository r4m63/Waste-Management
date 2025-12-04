//package ru.itmo.wastemanagement.config.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                .cors(Customizer.withDefaults())
//                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(
//                                "/api/public/**",
//                                "/auth/login",
//                                "/auth/register",
//                                "/v3/api-docs/**",
//                                "/swagger-ui.html",
//                                "/swagger-ui/**",
//                                "/actuator/health"
//                        ).permitAll()
//
//                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
//
//                        .requestMatchers("/api/driver/**").hasAnyRole("DRIVER", "COURIER", "WORKER")
//
//                        .anyRequest().authenticated()
//                )
//
//                .httpBasic(Customizer.withDefaults());
//
//        return http.build();
//    }
//}
