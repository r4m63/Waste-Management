package ru.itmo.wastemanagement.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KioskOrderCreateDto {
    @NotNull(message = "Garbage point ID is required")
    private Integer garbagePointId;

    @NotNull(message = "Container size ID is required")
    private Integer containerSizeId;

    private Integer userId;

    @NotNull(message = "Fraction ID is required")
    private Integer fractionId;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone; // Для создания или поиска пользователя по телефону
}

