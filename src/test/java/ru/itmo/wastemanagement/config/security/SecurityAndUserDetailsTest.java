package ru.itmo.wastemanagement.config.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.itmo.wastemanagement.entity.User;
import ru.itmo.wastemanagement.entity.enums.UserRole;
import ru.itmo.wastemanagement.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityAndUserDetailsTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void customUserDetailsExposesFields() {
        User user = new User();
        user.setId(1);
        user.setLogin("driver");
        user.setPassword("pwd");
        user.setRole(UserRole.DRIVER);
        user.setActive(false);

        CustomUserDetails details = new CustomUserDetails(user);

        assertThat(details.getId()).isEqualTo(1);
        assertThat(details.getUsername()).isEqualTo("driver");
        assertThat(details.getPassword()).isEqualTo("pwd");
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_DRIVER");
        assertThat(details.isEnabled()).isFalse();
        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void customUserDetailsServiceLoadsUser() {
        User user = new User();
        user.setLogin("admin");
        user.setRole(UserRole.ADMIN);
        user.setActive(true);
        when(userRepository.findByLogin("admin")).thenReturn(Optional.of(user));

        CustomUserDetailsService service = new CustomUserDetailsService(userRepository);
        var details = service.loadUserByUsername("admin");

        assertThat(details.getUsername()).isEqualTo("admin");
    }

    @Test
    void customUserDetailsServiceThrowsWhenMissingUser() {
        when(userRepository.findByLogin("missing")).thenReturn(Optional.empty());

        CustomUserDetailsService service = new CustomUserDetailsService(userRepository);

        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void securityConfigPasswordEncoderSupportsBcryptAndLegacyPlain() {
        SecurityConfig config = new SecurityConfig(mock(CustomUserDetailsService.class));
        PasswordEncoder encoder = config.passwordEncoder();

        String hashed = encoder.encode("secret");
        assertThat(encoder.matches("secret", hashed)).isTrue();
        assertThat(encoder.matches("secret", "secret")).isTrue();
        assertThat(encoder.matches("secret", null)).isFalse();
        assertThat(encoder.matches("secret", "wrong")).isFalse();
    }

    @Test
    void securityConfigProvidesAuthProviderAndManager() throws Exception {
        CustomUserDetailsService uds = mock(CustomUserDetailsService.class);
        SecurityConfig config = new SecurityConfig(uds);

        AuthenticationProvider provider = config.authenticationProvider();
        assertThat(provider).isInstanceOf(DaoAuthenticationProvider.class);

        AuthenticationManager manager = mock(AuthenticationManager.class);
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        when(authConfig.getAuthenticationManager()).thenReturn(manager);

        assertThat(config.authenticationManager(authConfig)).isSameAs(manager);
    }
}
