package ru.itmo.wastemanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.itmo.wastemanagement.dto.driver.DriverCreateUpdateDto;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.entity.User;
import ru.itmo.wastemanagement.entity.enums.UserRole;
import ru.itmo.wastemanagement.exception.BadRequestException;
import ru.itmo.wastemanagement.exception.ConflictException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.DriverGridRepository;
import ru.itmo.wastemanagement.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DriverGridRepository driverGridRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DriverService driverService;

    @Test
    void queryDriverGridReturnsRows() {
        GridTableRequest req = GridTableRequest.builder().startRow(0).endRow(10).build();
        User user = new User();
        user.setId(3);
        user.setName("Driver");
        when(driverGridRepository.findPageByGrid(req)).thenReturn(List.of(user));
        when(driverGridRepository.countByGrid(req)).thenReturn(1L);

        var result = driverService.queryDriverGrid(req);

        assertThat(result.getRows()).hasSize(1);
        assertThat(result.getRows().get(0).getId()).isEqualTo(3);
        assertThat(result.getLastRow()).isEqualTo(1);
    }

    @Test
    void createDriverThrowsOnDuplicateLogin() {
        DriverCreateUpdateDto dto = dto(" login ", "pass123");
        when(userRepository.existsByLogin("login")).thenReturn(true);

        assertThatThrownBy(() -> driverService.createDriver(dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createDriverThrowsWhenPasswordBlank() {
        DriverCreateUpdateDto dto = dto("login", "   ");
        when(userRepository.existsByLogin("login")).thenReturn(false);

        assertThatThrownBy(() -> driverService.createDriver(dto))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createDriverSavesTrimmedValues() {
        DriverCreateUpdateDto dto = dto(" login ", " pass123 ");
        dto.setName(" Driver Name ");
        dto.setPhone(" +7000 ");
        dto.setActive(null);
        when(userRepository.existsByLogin("login")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("ENC");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10);
            return u;
        });

        Integer id = driverService.createDriver(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User user = captor.getValue();
        assertThat(user.getRole()).isEqualTo(UserRole.DRIVER);
        assertThat(user.getLogin()).isEqualTo("login");
        assertThat(user.getName()).isEqualTo("Driver Name");
        assertThat(user.getPhone()).isEqualTo("+7000");
        assertThat(user.getPassword()).isEqualTo("ENC");
        assertThat(user.isActive()).isTrue();
        assertThat(id).isEqualTo(10);
    }

    @Test
    void updateDriverThrowsWhenNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.updateDriver(1, dto("a", "b")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateDriverThrowsWhenRoleMismatch() {
        User user = new User();
        user.setRole(UserRole.ADMIN);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> driverService.updateDriver(1, dto("a", "b")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateDriverThrowsOnLoginConflict() {
        User driver = new User();
        driver.setRole(UserRole.DRIVER);
        driver.setLogin("old");
        when(userRepository.findById(1)).thenReturn(Optional.of(driver));
        when(userRepository.existsByLogin("new")).thenReturn(true);

        DriverCreateUpdateDto dto = dto(" new ", " ");
        dto.setName("n");
        dto.setPhone("p");

        assertThatThrownBy(() -> driverService.updateDriver(1, dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateDriverUpdatesPasswordWhenProvided() {
        User driver = new User();
        driver.setRole(UserRole.DRIVER);
        driver.setLogin("old");
        driver.setActive(true);
        when(userRepository.findById(1)).thenReturn(Optional.of(driver));
        when(userRepository.existsByLogin("new")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("ENC2");

        DriverCreateUpdateDto dto = dto(" new ", " secret ");
        dto.setName(" Name ");
        dto.setPhone(" 123 ");
        dto.setActive(false);

        driverService.updateDriver(1, dto);

        assertThat(driver.getLogin()).isEqualTo("new");
        assertThat(driver.getName()).isEqualTo("Name");
        assertThat(driver.getPhone()).isEqualTo("123");
        assertThat(driver.getPassword()).isEqualTo("ENC2");
        assertThat(driver.isActive()).isFalse();
        verify(userRepository).save(driver);
    }

    @Test
    void deleteDriverThrowsWhenRoleMismatch() {
        User user = new User();
        user.setRole(UserRole.KIOSK);
        when(userRepository.findById(2)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> driverService.deleteDriver(2))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void deleteDriverDeletesEntity() {
        User user = new User();
        user.setRole(UserRole.DRIVER);
        when(userRepository.findById(2)).thenReturn(Optional.of(user));

        driverService.deleteDriver(2);

        verify(userRepository).delete(user);
    }

    private static DriverCreateUpdateDto dto(String login, String password) {
        DriverCreateUpdateDto dto = new DriverCreateUpdateDto();
        dto.setName("driver");
        dto.setPhone("123");
        dto.setLogin(login);
        dto.setPassword(password);
        dto.setActive(true);
        return dto;
    }
}
