//package ru.itmo.wastemanagement.controller;
//
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//// import org.springframework.security.crypto.password.PasswordEncoder; // Пока не используется
//import org.springframework.web.bind.annotation.*;
//import ru.itmo.wastemanagement.dto.user.UserCreateDto;
//import ru.itmo.wastemanagement.dto.user.UserDto;
//import ru.itmo.wastemanagement.entity.User;
//import ru.itmo.wastemanagement.entity.enums.UserRole;
//import ru.itmo.wastemanagement.exception.BadRequestException;
//import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
//import ru.itmo.wastemanagement.repository.UserRepository;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * REST Controller для аутентификации и авторизации.
// *
// * ПРИМЕЧАНИЕ: Это базовая реализация.
// * В production необходимо использовать JWT токены или OAuth2.
// */
//@RestController
//@RequestMapping("/api/auth")
//@RequiredArgsConstructor
//public class AuthController {
//
//    private final UserRepository userRepository;
//    // private final PasswordEncoder passwordEncoder; // Будет использоваться когда добавим пароли
//
//    /**
//     * Простой вход по телефону (без пароля для упрощения).
//     * В production должен быть полноценный вход с паролем/JWT.
//     */
//    @PostMapping("/login")
//    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
//        String phone = credentials.get("phone");
//
//        if (phone == null || phone.isEmpty()) {
//            throw new BadRequestException("Phone number is required");
//        }
//
//        User user = userRepository.findByPhone(phone)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found with phone: " + phone));
//
//        if (!user.isActive()) {
//            throw new BadRequestException("User account is inactive");
//        }
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("userId", user.getId());
//        response.put("role", user.getRole());
//        response.put("name", user.getName());
//        response.put("phone", user.getPhone());
//
//        // TODO: В production здесь должен быть JWT токен
//        response.put("token", "mock-token-" + user.getId());
//
//        return ResponseEntity.ok(response);
//    }
//
//    /**
//     * Регистрация нового пользователя (для residents).
//     */
//    @PostMapping("/register")
//    public ResponseEntity<UserDto> register(@Valid @RequestBody UserCreateDto dto) {
//        // При регистрации через публичный API можно создать только resident
//        if (dto.getRole() != null && dto.getRole() != UserRole.resident) {
//            throw new BadRequestException("Only resident role can be registered through public API");
//        }
//
//        if (userRepository.existsByPhone(dto.getPhone())) {
//            throw new BadRequestException("User with this phone already exists");
//        }
//
//        User user = new User();
//        user.setRole(UserRole.resident);
//        user.setPhone(dto.getPhone());
//        user.setName(dto.getName());
//        user.setActive(true);
//        user.setCreatedAt(LocalDateTime.now());
//
//        User saved = userRepository.save(user);
//
//        UserDto userDto = UserDto.builder()
//                .id(saved.getId())
//                .role(saved.getRole())
//                .phone(saved.getPhone())
//                .name(saved.getName())
//                .active(saved.isActive())
//                .createdAt(saved.getCreatedAt())
//                .build();
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
//    }
//
//    /**
//     * Получить информацию о текущем пользователе (по ID).
//     * В production должно быть через JWT токен.
//     */
//    @GetMapping("/me/{userId}")
//    public ResponseEntity<UserDto> getCurrentUser(@PathVariable Integer userId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
//
//        UserDto userDto = UserDto.builder()
//                .id(user.getId())
//                .role(user.getRole())
//                .phone(user.getPhone())
//                .name(user.getName())
//                .active(user.isActive())
//                .createdAt(user.getCreatedAt())
//                .build();
//
//        return ResponseEntity.ok(userDto);
//    }
//}
//
