package ru.itmo.wastemanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.dto.kiosk.KioskCreateUpdateDto;
import ru.itmo.wastemanagement.entity.User;
import ru.itmo.wastemanagement.entity.enums.UserRole;
import ru.itmo.wastemanagement.exception.BadRequestException;
import ru.itmo.wastemanagement.exception.ConflictException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.KioskGridRepository;
import ru.itmo.wastemanagement.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KioskServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KioskGridRepository kioskGridRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private KioskService kioskService;

    @Test
    void queryKioskGridReturnsRows() {
        GridTableRequest req = GridTableRequest.builder().startRow(0).endRow(10).build();
        User user = new User();
        user.setId(1);
        user.setName("Kiosk");
        when(kioskGridRepository.findPageByGrid(req)).thenReturn(List.of(user));
        when(kioskGridRepository.countByGrid(req)).thenReturn(1L);

        var result = kioskService.queryKioskGrid(req);

        assertThat(result.getRows()).hasSize(1);
        assertThat(result.getRows().get(0).getId()).isEqualTo(1);
        assertThat(result.getLastRow()).isEqualTo(1);
    }

    @Test
    void createKioskThrowsOnDuplicateLogin() {
        KioskCreateUpdateDto dto = dto(" kiosk ", "pass123");
        when(userRepository.existsByLogin(" kiosk ")).thenReturn(true);

        assertThatThrownBy(() -> kioskService.createKioskUser(dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createKioskSavesTrimmedFields() {
        KioskCreateUpdateDto dto = dto(" kiosk ", " pass123 ");
        dto.setName("  Kiosk Name ");
        dto.setActive(true);
        when(userRepository.existsByLogin(" kiosk ")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("ENC");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(55);
            return u;
        });

        Integer id = kioskService.createKioskUser(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getRole()).isEqualTo(UserRole.KIOSK);
        assertThat(saved.getLogin()).isEqualTo("kiosk");
        assertThat(saved.getName()).isEqualTo("Kiosk Name");
        assertThat(saved.getPassword()).isEqualTo("ENC");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(id).isEqualTo(55);
    }

    @Test
    void updateKioskThrowsWhenUserNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kioskService.updateKioskUser(1, dto("a", "b")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateKioskThrowsWhenRoleIsNotKiosk() {
        User u = new User();
        u.setRole(UserRole.DRIVER);
        when(userRepository.findById(1)).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> kioskService.updateKioskUser(1, dto("a", "b")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateKioskThrowsOnLoginConflict() {
        User u = new User();
        u.setRole(UserRole.KIOSK);
        u.setLogin("old");
        when(userRepository.findById(1)).thenReturn(Optional.of(u));
        KioskCreateUpdateDto dto = dto("new", "p");
        dto.setName("n");
        when(userRepository.existsByLogin("new")).thenReturn(true);

        assertThatThrownBy(() -> kioskService.updateKioskUser(1, dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateKioskUpdatesPasswordOnlyWhenPresent() {
        User u = new User();
        u.setRole(UserRole.KIOSK);
        u.setLogin("old");
        when(userRepository.findById(1)).thenReturn(Optional.of(u));
        KioskCreateUpdateDto dto = dto(" old ", "   ");
        dto.setName("  New Name ");
        dto.setActive(false);

        kioskService.updateKioskUser(1, dto);

        assertThat(u.getName()).isEqualTo("New Name");
        assertThat(u.isActive()).isFalse();
        verify(userRepository).save(u);
    }

    @Test
    void deleteKioskThrowsWhenRoleMismatch() {
        User u = new User();
        u.setRole(UserRole.ADMIN);
        when(userRepository.findById(1)).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> kioskService.deleteKioskUser(1))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void deleteKioskDeletesEntity() {
        User u = new User();
        u.setRole(UserRole.KIOSK);
        when(userRepository.findById(1)).thenReturn(Optional.of(u));

        kioskService.deleteKioskUser(1);

        verify(userRepository).delete(u);
    }

    private static KioskCreateUpdateDto dto(String login, String password) {
        KioskCreateUpdateDto dto = new KioskCreateUpdateDto();
        dto.setName("name");
        dto.setLogin(login);
        dto.setPassword(password);
        dto.setActive(true);
        return dto;
    }
}
