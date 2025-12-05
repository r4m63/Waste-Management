package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.dto.UserCreateDto;
import ru.itmo.wastemanagement.dto.UserDto;
import ru.itmo.wastemanagement.entity.User;
import ru.itmo.wastemanagement.entity.enums.UserRole;
import ru.itmo.wastemanagement.exception.ConflictException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDto> findByRole(UserRole role) {
        return userRepository.findByRole(role).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDto> findActiveDrivers() {
        return userRepository.findByRoleAndActiveTrue(UserRole.courier).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto findById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return toDto(user);
    }

    @Transactional(readOnly = true)
    public Optional<UserDto> findByPhone(String phone) {
        return userRepository.findByPhone(phone).map(this::toDto);
    }

    @Transactional
    public UserDto create(UserCreateDto dto) {
        if (dto.getPhone() != null && userRepository.existsByPhone(dto.getPhone())) {
            throw new ConflictException("User with phone '" + dto.getPhone() + "' already exists");
        }

        User user = new User();
        user.setRole(dto.getRole());
        user.setPhone(dto.getPhone());
        user.setName(dto.getName());
        user.setActive(dto.isActive());
        user.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        return toDto(saved);
    }

    @Transactional
    public UserDto createOrFindByPhone(String phone, String name) {
        Optional<User> existing = userRepository.findByPhone(phone);
        if (existing.isPresent()) {
            return toDto(existing.get());
        }

        User user = new User();
        user.setRole(UserRole.resident);
        user.setPhone(phone);
        user.setName(name);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        return toDto(saved);
    }

    @Transactional
    public UserDto update(Integer id, UserCreateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Проверяем уникальность phone если он изменился
        if (dto.getPhone() != null && !dto.getPhone().equals(user.getPhone())
                && userRepository.existsByPhone(dto.getPhone())) {
            throw new ConflictException("User with phone '" + dto.getPhone() + "' already exists");
        }

        user.setRole(dto.getRole());
        user.setPhone(dto.getPhone());
        user.setName(dto.getName());
        user.setActive(dto.isActive());

        User saved = userRepository.save(user);
        return toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .role(user.getRole())
                .phone(user.getPhone())
                .name(user.getName())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

