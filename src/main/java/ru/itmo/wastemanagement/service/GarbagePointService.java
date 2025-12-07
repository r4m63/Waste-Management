package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.wastemanagement.dto.GarbagePointDto;
import ru.itmo.wastemanagement.dto.GarbagePointRowDto;
import ru.itmo.wastemanagement.dto.GarbagePointUpsertDto;
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
        gp.setCreatedAt(LocalDateTime.now());
        if (dto.getAdminId() != null) {
            User adminRef = userRepository.getReferenceById(dto.getAdminId());
            gp.setAdmin(adminRef);
        }
        if (dto.getKioskId() != null) {
            User kioskRef = userRepository.getReferenceById(dto.getKioskId());
            gp.setKiosk(kioskRef);
        }

        garbagePointRepository.save(gp);
        return gp.getId();
    }


    @Transactional
    public void update(Integer id, GarbagePointUpsertDto dto) {
        GarbagePoint gp = garbagePointRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Garbage point not found"));

        // на всякий ставим id внутрь dto, если вдруг понадобится
        dto.setId(id);

        applyUpsertFields(gp, dto);

        garbagePointRepository.save(gp);
    }

    @Transactional
    public void delete(Integer id) {
        if (!garbagePointRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Garbage point not found");
        }
        garbagePointRepository.deleteById(id);
    }

    private void applyUpsertFields(GarbagePoint gp, GarbagePointUpsertDto dto) {
        gp.setAddress(dto.getAddress().trim());
        gp.setCapacity(dto.getCapacity());
        gp.setOpen(dto.getOpen() == null ? true : dto.getOpen());
        gp.setLat(dto.getLat());
        gp.setLon(dto.getLon());

        // admin
        gp.setAdmin(null);

        // kiosk
        if (dto.getKioskId() != null) {
            User kiosk = userRepository.findById(dto.getKioskId())
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kiosk user not found: " + dto.getKioskId()));
            gp.setKiosk(kiosk);
        } else {
            gp.setKiosk(null);
        }
    }


}