package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.itmo.wastemanagement.dto.driver.DriverCreateUpdateDto;
import ru.itmo.wastemanagement.dto.driver.DriverRowDto;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.dto.gridtable.GridTableResponse;
import ru.itmo.wastemanagement.entity.User;
import ru.itmo.wastemanagement.entity.enums.UserRole;
import ru.itmo.wastemanagement.exception.BadRequestException;
import ru.itmo.wastemanagement.exception.ConflictException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.DriverGridRepository;
import ru.itmo.wastemanagement.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final UserRepository userRepository;
    private final DriverGridRepository driverGridRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public GridTableResponse<DriverRowDto> queryDriverGrid(GridTableRequest req) {
        List<User> rows = driverGridRepository.findPageByGrid(req);
        long total = driverGridRepository.countByGrid(req);

        List<DriverRowDto> dtos = rows.stream()
                .map(DriverRowDto::fromEntity)
                .toList();

        return GridTableResponse.<DriverRowDto>builder()
                .rows(dtos)
                .lastRow((int) total)
                .build();
    }

    @Transactional
    public Integer createDriver(DriverCreateUpdateDto dto) {
        String login = dto.getLogin().trim();

        if (userRepository.existsByLogin(login)) {
            throw new ConflictException(
                    "Пользователь с логином '%s' уже существует".formatted(login)
            );
        }

        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new BadRequestException("Пароль обязателен для нового водителя");
        }

        User user = User.builder()
                .role(UserRole.DRIVER)
                .name(dto.getName().trim())
                .phone(dto.getPhone().trim())
                .login(login)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode(dto.getPassword().trim()))
                .build();

        return userRepository.save(user).getId();
    }

    @Transactional
    public void updateDriver(Integer id, DriverCreateUpdateDto dto) {
        User driver = userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(User.class, "id", id));

        if (driver.getRole() != UserRole.DRIVER) {
            throw new BadRequestException(
                    "Пользователь с id=%d не является водителем".formatted(id)
            );
        }

        String newLogin = dto.getLogin().trim();
        String newName = dto.getName().trim();
        String newPhone = dto.getPhone().trim();

        if (!newLogin.equals(driver.getLogin())) {
            if (userRepository.existsByLogin(newLogin)) {
                throw new ConflictException(
                        "Пользователь с логином '%s' уже существует".formatted(newLogin)
                );
            }
            driver.setLogin(newLogin);
        }

        driver.setName(newName);
        driver.setPhone(newPhone);

        if (dto.getActive() != null) {
            driver.setActive(dto.getActive());
        }

        // пароль меняем только если передан и не пустой
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            driver.setPassword(passwordEncoder.encode(dto.getPassword().trim()));
        }

        userRepository.save(driver); // можно и не вызывать, но так явно
    }

    @Transactional
    public void deleteDriver(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(User.class, "id", id));

        if (user.getRole() != UserRole.DRIVER) {
            throw new BadRequestException(
                    "Пользователь с id=%d не является водителем".formatted(id)
            );
        }

        userRepository.delete(user);
    }
}
