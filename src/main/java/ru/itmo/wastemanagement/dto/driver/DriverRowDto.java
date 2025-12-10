package ru.itmo.wastemanagement.dto.driver;

import lombok.Builder;
import lombok.Value;
import ru.itmo.wastemanagement.entity.User;

import java.time.LocalDateTime;

@Value
@Builder
public class DriverRowDto {

    Integer id;
    String name;
    String phone;
    String login;
    Boolean active;
    LocalDateTime createdAt;

    public static DriverRowDto fromEntity(User u) {
        if (u == null) return null;
        return DriverRowDto.builder()
                .id(u.getId())
                .name(u.getName())
                .phone(u.getPhone())
                .login(u.getLogin())
                .active(u.isActive())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
