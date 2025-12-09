package ru.itmo.wastemanagement.dto.kiosk;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KioskCreateUpdateDto {
    @NotBlank(message = "Имя киоска не может быть пустым")
    @Size(max = 255, message = "Имя киоска не должно превышать 255 символов")
    private String name;
    @NotBlank(message = "Логин не может быть пустым")
    @Size(max = 255, message = "Логин не должен превышать 255 символов")
    private String login;
    @Size(min = 6, max = 100, message = "Пароль должен быть от 6 до 100 символов")
    private String password;
    private Boolean active;
}
