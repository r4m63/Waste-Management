package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.dto.route.RouteDto;
import ru.itmo.wastemanagement.dto.route.RouteStopDto;
import ru.itmo.wastemanagement.entity.GarbagePoint;
import ru.itmo.wastemanagement.entity.Route;
import ru.itmo.wastemanagement.entity.RouteStop;
import ru.itmo.wastemanagement.entity.User;
import ru.itmo.wastemanagement.entity.enums.UserRole;
import ru.itmo.wastemanagement.entity.enums.RouteStatus;
import ru.itmo.wastemanagement.entity.enums.StopStatus;
import ru.itmo.wastemanagement.dto.route.RouteStopUpdateDto;
import ru.itmo.wastemanagement.exception.BadRequestException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.entity.DriverShift;
import ru.itmo.wastemanagement.entity.enums.ShiftStatus;
import ru.itmo.wastemanagement.repository.DriverShiftRepository;
import ru.itmo.wastemanagement.repository.GarbagePointRepository;
import ru.itmo.wastemanagement.repository.KioskOrderRepository;
import ru.itmo.wastemanagement.repository.RouteRepository;
import ru.itmo.wastemanagement.repository.RouteStopRepository;
import ru.itmo.wastemanagement.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {

    private static final double DEFAULT_FILL_THRESHOLD = 0.7; // >=70% заполнения

    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;
    private final KioskOrderRepository kioskOrderRepository;
    private final GarbagePointRepository garbagePointRepository;
    private final UserRepository userRepository;
    private final DriverShiftRepository driverShiftRepository;

    @Transactional(readOnly = true)
    public List<RouteDto> getAllRoutesWithStops() {
        List<Route> routes = routeRepository.findAll();
        if (routes.isEmpty()) {
            return List.of();
        }

        List<Integer> routeIds = routes.stream()
                .map(Route::getId)
                .toList();

        Map<Integer, List<RouteStop>> stopsByRoute = routeStopRepository
                .findByRoute_IdInOrderByRoute_IdAscSeqNoAsc(routeIds)
                .stream()
                .collect(Collectors.groupingBy(rs -> rs.getRoute().getId(), LinkedHashMap::new, Collectors.toList()));

        return routes.stream()
                .sorted(Comparator.comparing(Route::getPlannedDate).reversed().thenComparing(Route::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(r -> toDto(r, stopsByRoute.getOrDefault(r.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RouteDto> getRoutesForDriverLogin(String driverLogin) {
        if (driverLogin == null || driverLogin.isBlank()) {
            return List.of();
        }

        User driver = userRepository.findByLogin(driverLogin)
                .orElseThrow(() -> new ResourceNotFoundException("User", "login", driverLogin));

        if (driver.getRole() != UserRole.DRIVER) {
            throw new BadRequestException("Пользователь не является водителем: login=" + driverLogin);
        }

        List<Route> routes = routeRepository.findByDriver_Id(driver.getId());
        routes = routes.stream()
                .filter(r -> r.getStatus() == RouteStatus.planned || r.getStatus() == RouteStatus.in_progress)
                .toList();
        if (routes.isEmpty()) {
            return List.of();
        }

        List<Integer> routeIds = routes.stream()
                .map(Route::getId)
                .toList();

        Map<Integer, List<RouteStop>> stopsByRoute = routeStopRepository
                .findByRoute_IdInOrderByRoute_IdAscSeqNoAsc(routeIds)
                .stream()
                .collect(Collectors.groupingBy(rs -> rs.getRoute().getId(), LinkedHashMap::new, Collectors.toList()));

        return routes.stream()
                .sorted(Comparator.comparing(Route::getPlannedDate).reversed().thenComparing(Route::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(r -> toDto(r, stopsByRoute.getOrDefault(r.getId(), List.of())))
                .toList();
    }

    @Transactional
    public RouteDto acceptRoute(Integer routeId, String driverLogin) {
        if (routeId == null) {
            throw new BadRequestException("Не указан маршрут");
        }
        if (driverLogin == null || driverLogin.isBlank()) {
            throw new BadRequestException("Не указан водитель");
        }

        User driver = userRepository.findByLogin(driverLogin)
                .orElseThrow(() -> new ResourceNotFoundException("User", "login", driverLogin));

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", "id", routeId));

        if (route.getDriver() == null || !Objects.equals(route.getDriver().getId(), driver.getId())) {
            throw new BadRequestException("Маршрут не назначен этому водителю");
        }

        if (route.getStatus() != RouteStatus.planned) {
            throw new BadRequestException("Маршрут нельзя принять в статусе: " + route.getStatus());
        }

        route.setStatus(RouteStatus.in_progress);
        if (route.getStartedAt() == null) {
            route.setStartedAt(LocalDateTime.now());
        }
        routeRepository.save(route);

        List<RouteStop> stops = routeStopRepository.findByRoute_IdInOrderByRoute_IdAscSeqNoAsc(List.of(routeId));
        return toDto(route, stops);
    }

    @Transactional
    public RouteDto autoGenerateFromKioskOrders() {
        record PointLoad(double load, boolean hasWeight, boolean hasAnyOrders) {}
        Map<Integer, PointLoad> loadByPoint = new HashMap<>();

        for (Object[] row : kioskOrderRepository.findActiveWeightsByGarbagePoint()) {
            if (row == null || row.length < 5 || row[0] == null) {
                continue;
            }
            Integer gpId = ((Number) row[0]).intValue();
            double totalWeight = row[1] != null ? ((Number) row[1]).doubleValue() : 0d;
            long orderCount = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            long weightedCount = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            double totalContainerCapacity = row[4] != null ? ((Number) row[4]).doubleValue() : 0d;

            boolean hasWeight = weightedCount > 0;
            boolean hasAnyOrders = orderCount > 0;
            double load = hasWeight ? totalWeight : totalContainerCapacity;
            loadByPoint.put(gpId, new PointLoad(load, hasWeight, hasAnyOrders));
        }

        if (loadByPoint.isEmpty()) {
            throw new BadRequestException("Нет активных заказов киосков для создания маршрутов");
        }

        List<Map.Entry<GarbagePoint, PointLoad>> candidates = loadByPoint.entrySet().stream()
                .map(e -> garbagePointRepository.findById(e.getKey()).map(gp -> Map.entry(gp, e.getValue())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        // Primary selection: points with >= DEFAULT_FILL_THRESHOLD
        List<GarbagePoint> pointsToVisit = candidates.stream()
                .filter(entry -> needsCleanup(entry.getKey(), entry.getValue().load()))
                .map(Map.Entry::getKey)
                .toList();

        // Fallback: if nothing reaches threshold, still build a route for any open points with active orders
        if (pointsToVisit.isEmpty()) {
            pointsToVisit = candidates.stream()
                    .filter(entry -> entry.getKey() != null && entry.getKey().isOpen())
                    .filter(entry -> entry.getValue() != null && entry.getValue().hasAnyOrders())
                    .map(Map.Entry::getKey)
                    .toList();
        }

        if (pointsToVisit.isEmpty()) {
            boolean hasClosed = candidates.stream()
                    .anyMatch(entry -> entry.getKey() != null && !entry.getKey().isOpen() && entry.getValue() != null && entry.getValue().hasAnyOrders());
            if (hasClosed) {
                throw new BadRequestException("Нет точек для маршрута: все точки с активными заказами уже в маршрутах (is_open=false)");
            }
            throw new BadRequestException("Нет точек для маршрута (нет активных заказов или точки закрыты)");
        }

        Route route = Route.builder()
                .plannedDate(LocalDate.now())
                .build();
        route = routeRepository.save(route);

        int seq = 1;
        List<RouteStop> stops = new ArrayList<>();
        for (GarbagePoint gp : pointsToVisit) {
            PointLoad pl = loadByPoint.get(gp.getId());
            Double weight = pl != null ? pl.load() : 0d;
            RouteStop stop = RouteStop.builder()
                    .route(route)
                    .seqNo(seq++)
                    .garbagePoint(gp)
                    .address(gp.getAddress())
                    .expectedCapacity(weight != null ? (int) Math.round(weight) : null)
                    .build();
            stops.add(stop);
            gp.setOpen(false); // помечаем как "в обработке", чтобы не попадала в следующее авто-планирование
        }
        routeStopRepository.saveAll(stops);
        garbagePointRepository.saveAll(pointsToVisit);

        return toDto(route, stops);
    }

    @Transactional
    public void deleteRoute(Integer id) {
        if (!routeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Route", "id", id);
        }
        // reopen related points
        List<RouteStop> stops = routeStopRepository.findByRoute_IdInOrderByRoute_IdAscSeqNoAsc(List.of(id));
        List<GarbagePoint> gps = stops.stream()
                .map(RouteStop::getGarbagePoint)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        for (GarbagePoint gp : gps) {
            gp.setOpen(true);
        }
        garbagePointRepository.saveAll(gps);

        routeStopRepository.deleteByRoute_Id(id);
        routeRepository.deleteById(id);
    }

    @Transactional
    public RouteDto assignDriver(Integer routeId, Integer driverId, java.time.LocalDateTime plannedStart, java.time.LocalDateTime plannedEnd) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", "id", routeId));

        if (driverId != null) {
            User driver = userRepository.findById(driverId)
                    .orElseThrow(() -> ResourceNotFoundException.of(User.class, "id", driverId));
            if (driver.getRole() != UserRole.DRIVER && driver.getRole() != UserRole.ADMIN) {
                throw new BadRequestException("Пользователь не является водителем: id=" + driverId);
            }
            route.setDriver(driver);
        } else {
            route.setDriver(null);
        }

        route.setPlannedStartAt(plannedStart);
        route.setPlannedEndAt(plannedEnd);

        routeRepository.save(route);

        List<RouteStop> stops = routeStopRepository.findByRoute_IdInOrderByRoute_IdAscSeqNoAsc(List.of(routeId));
        return toDto(route, stops);
    }

    private boolean needsCleanup(GarbagePoint gp, Double weight) {
        if (!gp.isOpen()) {
            return false;
        }
        double totalWeight = weight != null ? weight : 0d;
        Integer capacity = gp.getCapacity();
        if (capacity == null || capacity <= 0) {
            return totalWeight > 0;
        }
        double fillRatio = totalWeight / capacity;
        return fillRatio >= DEFAULT_FILL_THRESHOLD;
    }

    private RouteDto toDto(Route route, List<RouteStop> stops) {
        List<RouteStopDto> stopDtos = stops.stream()
                .sorted(Comparator.comparing(RouteStop::getSeqNo))
                .map(RouteStopDto::fromEntity)
                .toList();

        return RouteDto.builder()
                .id(route.getId())
                .plannedDate(route.getPlannedDate())
                .status(route.getStatus())
                .plannedStartAt(route.getPlannedStartAt())
                .plannedEndAt(route.getPlannedEndAt())
                .startedAt(route.getStartedAt())
                .finishedAt(route.getFinishedAt())
                .driverId(route.getDriver() != null ? route.getDriver().getId() : null)
                .vehicleId(route.getVehicle() != null ? route.getVehicle().getId() : null)
                .shiftId(route.getShift() != null ? route.getShift().getId() : null)
                .stops(stopDtos)
                .build();
    }

    @Transactional(readOnly = true)
    public RouteDto getMyRoute(Integer routeId, String driverLogin) {
        if (routeId == null) {
            throw new BadRequestException("Не указан маршрут");
        }
        if (driverLogin == null || driverLogin.isBlank()) {
            throw new BadRequestException("Не указан водитель");
        }

        User driver = userRepository.findByLogin(driverLogin)
                .orElseThrow(() -> new ResourceNotFoundException("User", "login", driverLogin));

        if (driver.getRole() != UserRole.DRIVER) {
            throw new BadRequestException("Пользователь не является водителем: login=" + driverLogin);
        }

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", "id", routeId));

        if (route.getDriver() == null || !Objects.equals(route.getDriver().getId(), driver.getId())) {
            throw new BadRequestException("Маршрут не назначен этому водителю");
        }

        List<RouteStop> stops = routeStopRepository.findByRoute_IdInOrderByRoute_IdAscSeqNoAsc(List.of(routeId));
        return toDto(route, stops);
    }

    @Transactional
    public RouteDto startRoute(Integer routeId, String driverLogin) {
        if (routeId == null) {
            throw new BadRequestException("Не указан маршрут");
        }
        if (driverLogin == null || driverLogin.isBlank()) {
            throw new BadRequestException("Не указан водитель");
        }

        User driver = userRepository.findByLogin(driverLogin)
                .orElseThrow(() -> new ResourceNotFoundException("User", "login", driverLogin));

        if (driver.getRole() != UserRole.DRIVER) {
            throw new BadRequestException("Пользователь не является водителем: login=" + driverLogin);
        }

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", "id", routeId));

        if (route.getDriver() == null || !Objects.equals(route.getDriver().getId(), driver.getId())) {
            throw new BadRequestException("Маршрут не назначен этому водителю");
        }

        // Check if driver has an open shift
        DriverShift openShift = driverShiftRepository.findByDriver_IdAndStatus(driver.getId(), ShiftStatus.open)
                .orElse(null);
        if (openShift == null) {
            throw new BadRequestException("Для начала маршрута необходимо открыть смену");
        }

        // Link route to shift if not already linked
        if (route.getShift() == null) {
            route.setShift(openShift);
        }

        if (route.getStatus() == RouteStatus.completed || route.getStatus() == RouteStatus.cancelled) {
            throw new BadRequestException("Маршрут нельзя начать в статусе: " + route.getStatus());
        }

        if (route.getStatus() == RouteStatus.planned) {
            route.setStatus(RouteStatus.in_progress);
        }
        if (route.getStartedAt() == null) {
            route.setStartedAt(LocalDateTime.now());
        }
        routeRepository.save(route);

        List<RouteStop> stops = routeStopRepository.findByRoute_IdInOrderByRoute_IdAscSeqNoAsc(List.of(routeId));
        return toDto(route, stops);
    }

    @Transactional
    public RouteDto updateStop(Integer routeId, Integer stopId, String driverLogin, RouteStopUpdateDto dto) {
        if (routeId == null || stopId == null) {
            throw new BadRequestException("Не указан маршрут или остановка");
        }
        if (dto == null) {
            throw new BadRequestException("Не указаны данные обновления");
        }
        if (driverLogin == null || driverLogin.isBlank()) {
            throw new BadRequestException("Не указан водитель");
        }

        User driver = userRepository.findByLogin(driverLogin)
                .orElseThrow(() -> new ResourceNotFoundException("User", "login", driverLogin));

        if (driver.getRole() != UserRole.DRIVER) {
            throw new BadRequestException("Пользователь не является водителем: login=" + driverLogin);
        }

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", "id", routeId));

        if (route.getDriver() == null || !Objects.equals(route.getDriver().getId(), driver.getId())) {
            throw new BadRequestException("Маршрут не назначен этому водителю");
        }
        if (route.getStatus() != RouteStatus.in_progress) {
            throw new BadRequestException("Нельзя обновлять остановки, пока маршрут не в работе");
        }

        RouteStop stop = routeStopRepository.findById(stopId)
                .orElseThrow(() -> ResourceNotFoundException.of(RouteStop.class, "id", stopId));

        if (stop.getRoute() == null || !Objects.equals(stop.getRoute().getId(), routeId)) {
            throw new BadRequestException("Остановка не принадлежит маршруту");
        }

        StopStatus newStatus = dto.getStatus();
        stop.setStatus(newStatus);
        stop.setActualCapacity(dto.getActualCapacity());
        stop.setNote(dto.getNote());

        LocalDateTime now = LocalDateTime.now();
        if (stop.getTimeFrom() == null && newStatus != StopStatus.planned) {
            stop.setTimeFrom(now);
        }
        if ((newStatus == StopStatus.done || newStatus == StopStatus.skipped || newStatus == StopStatus.unavailable) && stop.getTimeTo() == null) {
            stop.setTimeTo(now);
        }

        routeStopRepository.save(stop);

        List<RouteStop> stops = routeStopRepository.findByRoute_IdInOrderByRoute_IdAscSeqNoAsc(List.of(routeId));
        boolean allDone = stops.stream().allMatch(s ->
                s.getStatus() == StopStatus.done || s.getStatus() == StopStatus.skipped || s.getStatus() == StopStatus.unavailable
        );
        if (allDone && route.getStatus() == RouteStatus.in_progress) {
            route.setStatus(RouteStatus.completed);
            if (route.getFinishedAt() == null) {
                route.setFinishedAt(now);
            }
            routeRepository.save(route);

            // reopen points so they can be planned again later
            List<GarbagePoint> gps = stops.stream()
                    .map(RouteStop::getGarbagePoint)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            for (GarbagePoint gp : gps) {
                gp.setOpen(true);
            }
            garbagePointRepository.saveAll(gps);
        }

        return toDto(route, stops);
    }

    @Transactional
    public RouteDto finishRoute(Integer routeId, String driverLogin) {
        if (routeId == null) {
            throw new BadRequestException("Не указан маршрут");
        }
        if (driverLogin == null || driverLogin.isBlank()) {
            throw new BadRequestException("Не указан водитель");
        }

        User driver = userRepository.findByLogin(driverLogin)
                .orElseThrow(() -> new ResourceNotFoundException("User", "login", driverLogin));

        if (driver.getRole() != UserRole.DRIVER) {
            throw new BadRequestException("Пользователь не является водителем: login=" + driverLogin);
        }

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", "id", routeId));

        if (route.getDriver() == null || !Objects.equals(route.getDriver().getId(), driver.getId())) {
            throw new BadRequestException("Маршрут не назначен этому водителю");
        }
        if (route.getStatus() != RouteStatus.in_progress) {
            throw new BadRequestException("Маршрут можно завершить только в статусе in_progress");
        }

        LocalDateTime now = LocalDateTime.now();

        List<RouteStop> stops = routeStopRepository.findByRoute_IdInOrderByRoute_IdAscSeqNoAsc(List.of(routeId));
        for (RouteStop stop : stops) {
            if (stop.getStatus() == StopStatus.done || stop.getStatus() == StopStatus.skipped || stop.getStatus() == StopStatus.unavailable) {
                continue;
            }
            stop.setStatus(StopStatus.skipped);
            if (stop.getTimeTo() == null) {
                stop.setTimeTo(now);
            }
        }
        routeStopRepository.saveAll(stops);

        route.setStatus(RouteStatus.completed);
        if (route.getFinishedAt() == null) {
            route.setFinishedAt(now);
        }
        if (route.getStartedAt() == null) {
            route.setStartedAt(now);
        }
        routeRepository.save(route);

        List<GarbagePoint> gps = stops.stream()
                .map(RouteStop::getGarbagePoint)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        for (GarbagePoint gp : gps) {
            gp.setOpen(true);
        }
        garbagePointRepository.saveAll(gps);

        // return latest snapshot
        stops = routeStopRepository.findByRoute_IdInOrderByRoute_IdAscSeqNoAsc(List.of(routeId));
        return toDto(route, stops);
    }
}
