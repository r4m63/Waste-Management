package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.dto.garbagepoint.GarbagePointCreateUpdateDto;
import ru.itmo.wastemanagement.dto.garbagepoint.GarbagePointRowDto;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.dto.gridtable.GridTableResponse;
import ru.itmo.wastemanagement.entity.GarbagePoint;
import ru.itmo.wastemanagement.entity.User;
import ru.itmo.wastemanagement.exception.BadRequestException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
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
    public Integer createNewGarbagePoint(GarbagePointCreateUpdateDto dto) {
        GarbagePoint gp = GarbagePointCreateUpdateDto.toEntity(dto, null);
        gp.setCreatedAt(LocalDateTime.now());

        if (dto.getKioskId() != null) {
            User kiosk = userRepository.findById(dto.getKioskId())
                    .orElseThrow(() -> new BadRequestException("Kiosk user not found: " + dto.getKioskId()));
            gp.setKiosk(kiosk);
        }

        return garbagePointRepository.save(gp).getId();
    }


    @Transactional
    public void update(Integer id, GarbagePointCreateUpdateDto dto) {
        GarbagePoint gp = garbagePointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GarbagePoint", "id", id));

        gp.setAddress(dto.getAddress().trim());
        gp.setCapacity(dto.getCapacity());
        gp.setOpen(dto.getOpen() != null ? dto.getOpen() : true);
        gp.setLat(dto.getLat());
        gp.setLon(dto.getLon());

        if (dto.getKioskId() != null) {
            User kiosk = userRepository.findById(dto.getKioskId())
                    .orElseThrow(() -> new BadRequestException("Kiosk user not found: " + dto.getKioskId()));
            gp.setKiosk(kiosk);
        } else {
            gp.setKiosk(null);
        }

        garbagePointRepository.save(gp);
    }

    @Transactional
    public void delete(Integer id) {
        GarbagePoint gp = garbagePointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GarbagePoint", "id", id));
        garbagePointRepository.delete(gp);
    }

}