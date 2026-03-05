package ru.itmo.wastemanagement.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class GridTablePredicateBuilderTest {

    @Test
    void parseToLocalDateHandlesNullBlankAndValidValue() {
        assertThat(GridTablePredicateBuilder.parseToLocalDate(null)).isNull();
        assertThat(GridTablePredicateBuilder.parseToLocalDate(" ")).isNull();
        assertThat(GridTablePredicateBuilder.parseToLocalDate("2026-03-05")).isEqualTo(LocalDate.of(2026, 3, 5));
    }
}
