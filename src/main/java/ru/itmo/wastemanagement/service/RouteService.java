package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.dto.route.RouteDto;
import ru.itmo.wastemanagement.dto.route.RouteStopDto;
import ru.itmo.wastemanagement.entity.GarbagePoint;
import ru.itmo.wastemanagement.entity.Route;
import ru.itmo.wastemanagement.entity.RouteStop;
import ru.itmo.wastemanagement.exception.BadRequestException;
import ru.itmo.wastemanagement.repository.GarbagePointRepository;
import ru.itmo.wastemanagement.repository.KioskOrderRepository;
import ru.itmo.wastemanagement.repository.RouteRepository;
import ru.itmo.wastemanagement.repository.RouteStopRepository;

import java.time.LocalDate;
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

    @Transactional
    public RouteDto autoGenerateFromKioskOrders() {
        Map<Integer, Double> weightByPoint = new HashMap<>();
        for (Object[] row : kioskOrderRepository.findActiveWeightsByGarbagePoint()) {
            if (row == null || row.length < 2 || row[0] == null || row[1] == null) {
                continue;
            }
            Integer gpId = ((Number) row[0]).intValue();
            Double weight = ((Number) row[1]).doubleValue();
            weightByPoint.put(gpId, weight);
        }

        if (weightByPoint.isEmpty()) {
            throw new BadRequestException("Нет активных заказов киосков для создания маршрутов");
        }

        List<GarbagePoint> pointsToVisit = weightByPoint.entrySet().stream()
                .map(e -> garbagePointRepository.findById(e.getKey()).map(gp -> Map.entry(gp, e.getValue())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(entry -> needsCleanup(entry.getKey(), entry.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        if (pointsToVisit.isEmpty()) {
            throw new BadRequestException("Не найдено точек с заполнением более 70%");
        }

        Route route = Route.builder()
                .plannedDate(LocalDate.now())
                .build();
        route = routeRepository.save(route);

        int seq = 1;
        List<RouteStop> stops = new ArrayList<>();
        for (GarbagePoint gp : pointsToVisit) {
            Double weight = weightByPoint.getOrDefault(gp.getId(), 0d);
            RouteStop stop = RouteStop.builder()
                    .route(route)
                    .seqNo(seq++)
                    .garbagePoint(gp)
                    .address(gp.getAddress())
                    .expectedCapacity(weight != null ? (int) Math.round(weight) : null)
                    .build();
            stops.add(stop);
        }
        routeStopRepository.saveAll(stops);

        return toDto(route, stops);
    }

    private boolean needsCleanup(GarbagePoint gp, Double weight) {
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
}
