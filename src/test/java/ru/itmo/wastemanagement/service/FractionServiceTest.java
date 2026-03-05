package ru.itmo.wastemanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.wastemanagement.dto.fraction.FractionUpsertDto;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.entity.Fraction;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.FractionGridRepository;
import ru.itmo.wastemanagement.repository.FractionRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FractionServiceTest {

    @Mock
    private FractionRepository fractionRepository;

    @Mock
    private FractionGridRepository fractionGridRepository;

    @InjectMocks
    private FractionService fractionService;

    @Test
    void queryFractionGridMapsRows() {
        GridTableRequest req = GridTableRequest.builder().startRow(0).endRow(50).build();
        Fraction f = new Fraction();
        f.setId(1);
        f.setName("Paper");
        f.setCode("PAPER");
        when(fractionGridRepository.findPageByGrid(req)).thenReturn(List.of(f));
        when(fractionGridRepository.countByGrid(req)).thenReturn(1L);

        var result = fractionService.queryFractionGrid(req);

        assertThat(result.getRows()).hasSize(1);
        assertThat(result.getRows().get(0).getCode()).isEqualTo("PAPER");
        assertThat(result.getLastRow()).isEqualTo(1);
    }

    @Test
    void createFractionReturnsId() {
        FractionUpsertDto dto = dto("Glass", "GLASS");
        when(fractionRepository.save(any(Fraction.class))).thenAnswer(inv -> {
            Fraction saved = inv.getArgument(0);
            saved.setId(22);
            return saved;
        });

        Integer id = fractionService.createFraction(dto);

        assertThat(id).isEqualTo(22);
    }

    @Test
    void updateFractionThrowsWhenNotFound() {
        when(fractionRepository.findById(7)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fractionService.updateFraction(7, dto(" A ", " B ")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateFractionUpdatesTrimmedFields() {
        Fraction fraction = new Fraction();
        fraction.setId(3);
        when(fractionRepository.findById(3)).thenReturn(Optional.of(fraction));

        FractionUpsertDto dto = dto("  Metal  ", "  METAL  ");
        dto.setDescription("desc");
        dto.setHazardous(true);

        fractionService.updateFraction(3, dto);

        assertThat(fraction.getName()).isEqualTo("Metal");
        assertThat(fraction.getCode()).isEqualTo("METAL");
        assertThat(fraction.isHazardous()).isTrue();
        verify(fractionRepository).save(fraction);
    }

    @Test
    void deleteFractionDeletesEntity() {
        Fraction fraction = new Fraction();
        when(fractionRepository.findById(5)).thenReturn(Optional.of(fraction));

        fractionService.deleteFraction(5);

        verify(fractionRepository).delete(fraction);
    }

    private static FractionUpsertDto dto(String name, String code) {
        FractionUpsertDto dto = new FractionUpsertDto();
        dto.setName(name);
        dto.setCode(code);
        return dto;
    }
}
