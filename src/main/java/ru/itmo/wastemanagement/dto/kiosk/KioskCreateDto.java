package ru.itmo.wastemanagement.dto.kiosk;

import lombok.Data;

@Data
public class KioskCreateDto {
    private String name;
    private String login;
    private String password;
    private Boolean active;
}
