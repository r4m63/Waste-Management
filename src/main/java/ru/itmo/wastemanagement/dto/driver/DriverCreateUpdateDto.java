package ru.itmo.wastemanagement.dto.driver;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverCreateUpdateDto {

    @NotBlank(message = "Имя водителя не может быть пустым")
    @Size(max = 255, message = "Имя водителя не должно превышать 255 символов")
    private String name;

    @NotBlank(message = "Телефон не может быть пустым")
    @Size(max = 50, message = "Телефон не должен превышать 50 символов")
    private String phone;

    @NotBlank(message = "Логин не может быть пустым")
    @Size(max = 255, message = "Логин не должен превышать 255 символов")
    private String login;

    @Size(min = 6, max = 100, message = "Пароль должен быть от 6 до 100 символов")
    private String password;

    private Boolean active;
}
