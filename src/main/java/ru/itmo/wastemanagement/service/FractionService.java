package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.dto.fraction.FractionRowDto;
import ru.itmo.wastemanagement.dto.fraction.FractionUpsertDto;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.dto.gridtable.GridTableResponse;
import ru.itmo.wastemanagement.entity.Fraction;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.FractionRepository;
import ru.itmo.wastemanagement.repository.FractionGridRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FractionService {

    private final FractionRepository fractionRepository;
    private final FractionGridRepository fractionGridRepository;

    @Transactional(readOnly = true)
    public GridTableResponse<FractionRowDto> queryFractionGrid(GridTableRequest req) {
        List<Fraction> rows = fractionGridRepository.findPageByGrid(req);
        long total = fractionGridRepository.countByGrid(req);

        List<FractionRowDto> dtos = rows.stream()
                .map(FractionRowDto::fromEntity)
                .toList();

        return GridTableResponse.<FractionRowDto>builder()
                .rows(dtos)
                .lastRow((int) total)
                .build();
    }

    @Transactional
    public Integer createFraction(FractionUpsertDto dto) {
        Fraction fraction = Fraction.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .description(dto.getDescription())
                .hazardous(dto.isHazardous())
                .build();
        return fractionRepository.save(fraction).getId();
    }


    @Transactional
    public void updateFraction(Integer id, FractionUpsertDto dto) {
        Fraction fraction = fractionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(Fraction.class, "id", id));

        fraction.setName(dto.getName().trim());
        fraction.setCode(dto.getCode().trim());
        fraction.setDescription(dto.getDescription());
        fraction.setHazardous(dto.isHazardous());

        fractionRepository.save(fraction);
    }

    @Transactional
    public void deleteFraction(Integer id) {
        Fraction fraction = fractionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(Fraction.class, "id", id));

        fractionRepository.delete(fraction);
    }
}
