package ru.itmo.wastemanagement.dto.kiosk;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KioskCreateDto {
    @NotNull(message = "Заполните name.")
    private String name;
    @NotNull(message = "Заполните login.")
    private String login;
    @NotNull(message = "Заполните password.")
    private String password;
    @NotNull(message = "Заполните active.")
    private Boolean active;
}
