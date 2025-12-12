package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.dto.gridtable.GridTableResponse;
import ru.itmo.wastemanagement.dto.kiosk.KioskCreateUpdateDto;
import ru.itmo.wastemanagement.dto.kiosk.KioskRowDto;
import ru.itmo.wastemanagement.entity.User;
import ru.itmo.wastemanagement.entity.enums.UserRole;
import ru.itmo.wastemanagement.exception.BadRequestException;
import ru.itmo.wastemanagement.exception.ConflictException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.KioskGridRepository;
import ru.itmo.wastemanagement.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KioskService {

    private final UserRepository userRepository;
    private final KioskGridRepository kioskGridRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public GridTableResponse<KioskRowDto> queryKioskGrid(GridTableRequest req) {
        List<User> rows = kioskGridRepository.findPageByGrid(req);
        long total = kioskGridRepository.countByGrid(req);

        List<KioskRowDto> dtos = rows.stream()
                .map(KioskRowDto::fromEntity)
                .toList();

        return GridTableResponse.<KioskRowDto>builder()
                .rows(dtos)
                .lastRow((int) total)
                .build();
    }

    @Transactional
    public Integer createKioskUser(KioskCreateUpdateDto dto) {
        if (userRepository.existsByLogin(dto.getLogin()))
            throw new ConflictException("Пользователь с логином '%s' уже существует".formatted(dto.getLogin()));

        User user = User.builder()
                .role(UserRole.KIOSK)
                .name(dto.getName().trim())
                .login(dto.getLogin().trim())
                .active(dto.getActive())
                .createdAt(LocalDateTime.now())
                .password(passwordEncoder.encode(dto.getPassword().trim()))
                .build();
        return userRepository.save(user).getId();
    }

    @Transactional
    public void updateKioskUser(Integer id, KioskCreateUpdateDto dto) {
        User kiosk = userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(User.class, "id", id));

        // теоретически не может поменяться и проверка излишняя
        if (kiosk.getRole() != UserRole.KIOSK)
            throw new BadRequestException("Пользователь с id=%d не является киоском".formatted(id));

        String newLogin = dto.getLogin().trim();
        String newName = dto.getName().trim();

        if (!newLogin.equals(kiosk.getLogin())) {
            if (userRepository.existsByLogin(newLogin))
                throw new ConflictException("Пользователь с логином '%s' уже существует".formatted(newLogin));
            kiosk.setLogin(newLogin);
        }

        kiosk.setName(newName);

        if (dto.getActive() != null)
            kiosk.setActive(dto.getActive());

        if (dto.getPassword() != null && !dto.getPassword().isBlank())
            kiosk.setPassword(passwordEncoder.encode(dto.getPassword().trim()));

        userRepository.save(kiosk);
    }

    @Transactional
    public void deleteKioskUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(User.class, "id", id));

        if (user.getRole() != UserRole.KIOSK)
            throw new BadRequestException("Пользователь с id=%d не является киоском".formatted(id));

        userRepository.delete(user);
    }

}
