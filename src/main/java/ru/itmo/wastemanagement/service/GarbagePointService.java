package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.dto.GarbagePointDto;
import ru.itmo.wastemanagement.dto.GarbagePointRowDto;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.dto.gridtable.GridTableResponse;
import ru.itmo.wastemanagement.entity.GarbagePoint;
import ru.itmo.wastemanagement.entity.User;
import ru.itmo.wastemanagement.repository.GarbagePointGridRepository;
import ru.itmo.wastemanagement.repository.GarbagePointRepository;
import ru.itmo.wastemanagement.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GarbagePointService {

    private final GarbagePointRepository garbagePointRepository;
    private final GarbagePointGridRepository gridRepository;
    private final UserRepository userRepository;


    @Transactional(readOnly = true)
    public GridTableResponse<GarbagePointRowDto> queryGrid(GridTableRequest req) {
        List<GarbagePoint> rows = gridRepository.findPageByGrid(req);
        long total = gridRepository.countByGrid(req);

        List<GarbagePointRowDto> dtos = rows.stream()
                .map(GarbagePointRowDto::fromEntity)
                .toList();

        return GridTableResponse.<GarbagePointRowDto>builder()
                .rows(dtos)
                .lastRow((int) total)
                .build();
    }

    @Transactional
    public Integer createNewGarbagePoint(GarbagePointDto dto) {
        GarbagePoint gp = GarbagePointDto.toEntity(dto, null);

        // createdAt — сейчас
        gp.setCreatedAt(LocalDateTime.now());

        // если передан adminId — подтягиваем ссылку
        if (dto.getAdminId() != null) {
            User adminRef = userRepository.getReferenceById(dto.getAdminId());
            gp.setAdmin(adminRef);
        }

        // если передан kioskId — подтягиваем ссылку
        if (dto.getKioskId() != null) {
            User kioskRef = userRepository.getReferenceById(dto.getKioskId());
            gp.setKiosk(kioskRef);
        }

        garbagePointRepository.save(gp);
        return gp.getId();
    }

}