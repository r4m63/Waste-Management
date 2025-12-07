package ru.itmo.wastemanagement.dto.kiosk;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KioskUpdateDto {

    @NotBlank
    private String name;

    @NotBlank
    private String login;

    private String password;

    private Boolean active;
}
