package ru.itmo.wastemanagement.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.UserCreateDto;
import ru.itmo.wastemanagement.dto.UserDto;
import ru.itmo.wastemanagement.entity.enums.UserRole;
import ru.itmo.wastemanagement.service.UserService;

import java.util.List;

/**
 * Admin REST Controller для управления пользователями.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    /**
     * Получить всех пользователей.
     */
    @GetMapping
    public ResponseEntity<List<UserDto>> getAll() {
        List<UserDto> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    /**
     * Получить пользователей по роли.
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserDto>> getByRole(@PathVariable UserRole role) {
        List<UserDto> users = userService.findByRole(role);
        return ResponseEntity.ok(users);
    }

    /**
     * Получить активных водителей.
     */
    @GetMapping("/drivers/active")
    public ResponseEntity<List<UserDto>> getActiveDrivers() {
        List<UserDto> drivers = userService.findActiveDrivers();
        return ResponseEntity.ok(drivers);
    }

    /**
     * Получить пользователя по ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable Integer id) {
        UserDto user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Создать нового пользователя.
     */
    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody UserCreateDto dto) {
        UserDto created = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновить пользователя.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable Integer id, @RequestBody UserCreateDto dto) {
        UserDto updated = userService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Удалить пользователя.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

