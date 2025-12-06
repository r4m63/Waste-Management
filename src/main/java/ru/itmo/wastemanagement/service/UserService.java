package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.dto.gridtable.GridTableResponse;
import ru.itmo.wastemanagement.dto.kiosk.KioskCreateDto;
import ru.itmo.wastemanagement.dto.kiosk.KioskRowDto;
import ru.itmo.wastemanagement.entity.User;
import ru.itmo.wastemanagement.entity.enums.UserRole;
import ru.itmo.wastemanagement.repository.KioskGridRepository;
import ru.itmo.wastemanagement.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final KioskGridRepository kioskGridRepository;

    @Transactional
    public Integer createKioskUser(KioskCreateDto dto) {
        if (userRepository.existsByLogin(dto.getLogin()))
            throw new IllegalArgumentException("Пользователь с таким логином уже существует"); // TODO: свой Exception + @ControllerAdvice с 409/400

        User user = new User();
        user.setRole(UserRole.KIOSK);
        user.setName(dto.getName().trim());
        user.setLogin(dto.getLogin().trim());
        user.setActive(dto.getActive() == null || dto.getActive());
        user.setCreatedAt(LocalDateTime.now());
        user.setPassword(dto.getPassword().trim()); // TODO: заменить на passwordEncoder.encode(req.getPassword())

        User saved = userRepository.save(user);

        return saved.getId();
    }

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

}
