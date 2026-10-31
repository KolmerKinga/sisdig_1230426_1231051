package pt.isep.aisafe.routeflight.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.aisafe.routeflight.api.dto.CreateRouteRequest;
import pt.isep.aisafe.routeflight.api.dto.RouteResponse;
import pt.isep.aisafe.routeflight.application.RouteExportService;
import pt.isep.aisafe.routeflight.application.RouteSearchService;
import pt.isep.aisafe.routeflight.application.RouteService;
import pt.isep.aisafe.routeflight.domain.Route;
import pt.isep.aisafe.routeflight.domain.RouteRequirements;
import pt.isep.aisafe.routeflight.domain.RouteStatus;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;
    private final RouteSearchService routeSearchService;
    private final RouteExportService routeExportService;

    public RouteController(RouteService routeService,
                           RouteSearchService routeSearchService,
                           RouteExportService routeExportService) {
        this.routeService = routeService;
        this.routeSearchService = routeSearchService;
        this.routeExportService = routeExportService;
    }

    @PostMapping
    public ResponseEntity<RouteResponse> createRoute(@Valid @RequestBody CreateRouteRequest request) {
        RouteRequirements requirements = new RouteRequirements(request.requiredRange(), request.requiredCapacity());

        Route route = routeService.createRoute(
                request.routeName(),
                request.type(),
                request.originIata(),
                request.destinationIata(),
                request.estimatedTimeHours(),
                request.distanceKm(),
                requirements
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(RouteResponse.fromDomain(route));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> getRouteById(@PathVariable Long id) {
        Route route = routeService.getRouteById(id);
        return ResponseEntity.ok(RouteResponse.fromDomain(route));
    }

    @GetMapping
    public ResponseEntity<List<RouteResponse>> getRoutes(
            @RequestParam(required = false) RouteStatus status,
            @RequestParam(required = false) String airportIata) {

        List<Route> routes;
        if (status != null) {
            routes = routeSearchService.findByStatus(status);
        } else if (airportIata != null && !airportIata.isBlank()) {
            routes = routeSearchService.findByAirport(airportIata);
        } else {
            routes = routeSearchService.findAllRoutes();
        }

        List<RouteResponse> response = routes.stream()
                .map(RouteResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<RouteResponse> activateRoute(@PathVariable Long id) {
        Route route = routeService.activateRoute(id);
        return ResponseEntity.ok(RouteResponse.fromDomain(route));
    }

    @PatchMapping("/{id}/suspend")
    public ResponseEntity<RouteResponse> suspendRoute(@PathVariable Long id) {
        Route route = routeService.suspendRoute(id);
        return ResponseEntity.ok(RouteResponse.fromDomain(route));
    }

    @PatchMapping("/{id}/retire")
    public ResponseEntity<RouteResponse> retireRoute(@PathVariable Long id) {
        Route route = routeService.retireRoute(id);
        return ResponseEntity.ok(RouteResponse.fromDomain(route));
    }

    @GetMapping("/export/csv")
    public ResponseEntity<String> exportCsv() {
        String csvData = routeExportService.exportRoutesToCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=routes.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
