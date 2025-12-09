package ru.itmo.wastemanagement.dto.kiosk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.wastemanagement.entity.User;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KioskRowDto {

    private Integer id;
    private String name;
    private String login;
    private Boolean active;
    private LocalDateTime createdAt;

    public static KioskRowDto fromEntity(User u) {
        if (u == null) return null;
        return KioskRowDto.builder()
                .id(u.getId())
                .name(u.getName())
                .login(u.getLogin())
                .active(u.isActive())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
