package ru.itmo.wastemanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.entity.GarbagePoint;
import ru.itmo.wastemanagement.repository.GarbagePointRepository;
import ru.itmo.wastemanagement.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/garbage-point")
public class GarbagePointController {

    private final GarbagePointRepository garbagePointRepository;
    private final UserRepository userRepository;

    public GarbagePointController(GarbagePointRepository garbagePointRepository, UserRepository userRepository) {
        this.garbagePointRepository = garbagePointRepository;
        this.userRepository = userRepository;
    }

//    @GetMapping
//    public ResponseEntity<List<GarbagePoint>> getAll() {
//        List<GarbagePoint> garbagePoints = garbagePointRepository.findAll();
//        return ResponseEntity.ok(garbagePoints);
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<GarbagePoint> getById(@PathVariable("id") Integer id) {
//        if (id == null) {
//            return ResponseEntity.badRequest().build();
//        }
//        Optional<GarbagePoint> garbagePoint = garbagePointRepository.findById(id);
//        return garbagePoint.map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @PostMapping
//    public ResponseEntity<GarbagePoint> create(@RequestBody GarbagePoint garbagePoint) {
//        // Устанавливаем createdAt при создании
//        garbagePoint.setCreatedAt(LocalDateTime.now());
//        // Игнорируем id, если передан (будет сгенерирован автоматически)
//        garbagePoint.setId(null);
//
//        // Если передан admin, проверяем его существование
//        if (garbagePoint.getAdmin() != null && garbagePoint.getAdmin().getId() != null) {
//            Optional<ru.itmo.wastemanagement.entity.User> admin = userRepository.findById(garbagePoint.getAdmin().getId());
//            if (admin.isEmpty()) {
//                return ResponseEntity.badRequest().build();
//            }
//            garbagePoint.setAdmin(admin.get());
//        }
//
//        GarbagePoint saved = garbagePointRepository.save(garbagePoint);
//        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<GarbagePoint> update(@PathVariable("id") Integer id, @RequestBody GarbagePoint garbagePoint) {
//        if (id == null) {
//            return ResponseEntity.badRequest().build();
//        }
//        Optional<GarbagePoint> existing = garbagePointRepository.findById(id);
//        if (existing.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        GarbagePoint existingPoint = existing.get();
//
//        // Обновляем поля
//        existingPoint.setAddress(garbagePoint.getAddress());
//        existingPoint.setCapacity(garbagePoint.getCapacity());
//        existingPoint.setOpen(garbagePoint.isOpen());
//        existingPoint.setLat(garbagePoint.getLat());
//        existingPoint.setLon(garbagePoint.getLon());
//
//        // Обновляем admin, если передан
//        if (garbagePoint.getAdmin() != null && garbagePoint.getAdmin().getId() != null) {
//            Optional<ru.itmo.wastemanagement.entity.User> admin = userRepository.findById(garbagePoint.getAdmin().getId());
//            if (admin.isEmpty()) {
//                return ResponseEntity.badRequest().build();
//            }
//            existingPoint.setAdmin(admin.get());
//        } else if (garbagePoint.getAdmin() == null) {
//            existingPoint.setAdmin(null);
//        }
//
//        // createdAt не обновляем - это поле только для создания
//
//        GarbagePoint updated = garbagePointRepository.save(existingPoint);
//        return ResponseEntity.ok(updated);
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable("id") Integer id) {
//        if (id == null) {
//            return ResponseEntity.badRequest().build();
//        }
//        if (!garbagePointRepository.existsById(id)) {
//            return ResponseEntity.notFound().build();
//        }
//        garbagePointRepository.deleteById(id);
//        return ResponseEntity.noContent().build();
//    }

}
