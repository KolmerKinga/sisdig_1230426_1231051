package pt.isep.aisafe.route_flight.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import pt.isep.aisafe.route_flight.domain.Route;
import pt.isep.aisafe.route_flight.domain.RouteRequirements;
import pt.isep.aisafe.route_flight.domain.RouteType;
import pt.isep.aisafe.route_flight.shared.config.AisafeProperties;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class RouteFlightBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RouteFlightBootstrap.class);

    private final AisafeProperties aisafeProperties;
    private final ResourceLoader resourceLoader;
    private final RouteService routeService;
    private final RouteSearchService routeSearchService;
    private final FlightService flightService;
    private final ObjectMapper objectMapper;

    public RouteFlightBootstrap(AisafeProperties aisafeProperties,
                                ResourceLoader resourceLoader,
                                RouteService routeService,
                                RouteSearchService routeSearchService,
                                FlightService flightService,
                                ObjectMapper objectMapper) {
        this.aisafeProperties = aisafeProperties;
        this.resourceLoader = resourceLoader;
        this.routeService = routeService;
        this.routeSearchService = routeSearchService;
        this.flightService = flightService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) {
        String seedFile = aisafeProperties.getSeedFile();
        if (seedFile == null || seedFile.isBlank()) {
            return;
        }

        try {
            Resource resource = resourceLoader.getResource(seedFile);
            if (!resource.exists()) {
                log.info("Ficheiro de seed nao encontrado: {}", seedFile);
                return;
            }

            try (InputStream inputStream = resource.getInputStream()) {
                SeedData seedData = objectMapper.readValue(inputStream, SeedData.class);
                loadSeedData(seedData);
            }
        } catch (Exception e) {
            log.error("Erro ao carregar ficheiro de seed {}: {}", seedFile, e.getMessage());
        }
    }

    private void loadSeedData(SeedData seedData) {
        if (seedData.routes() != null) {
            for (SeedRoute sr : seedData.routes()) {
                try {
                    RouteRequirements reqs = new RouteRequirements(sr.requiredRange(), sr.requiredCapacity());
                    RouteType type = RouteType.valueOf(sr.type());
                    routeService.createRoute(
                            sr.routeName(),
                            type,
                            sr.originIata(),
                            sr.destinationIata(),
                            sr.estimatedTimeHours(),
                            sr.distanceKm(),
                            reqs
                    );
                } catch (Exception e) {
                    log.warn("Erro ao carregar rota seed {}: {}", sr.routeName(), e.getMessage());
                }
            }
        }

        if (seedData.flights() != null) {
            for (SeedFlight sf : seedData.flights()) {
                try {
                    Route route = routeSearchService.findByRouteName(sf.routeName()).orElse(null);
                    if (route != null) {
                        flightService.scheduleFlight(
                                route.getRouteId(),
                                sf.aircraftRegistration(),
                                LocalDateTime.parse(sf.scheduledDeparture()),
                                LocalDateTime.parse(sf.scheduledArrival())
                        );
                    }
                } catch (Exception e) {
                    log.warn("Erro ao carregar voo seed para rota {}: {}", sf.routeName(), e.getMessage());
                }
            }
        }
    }

    private record SeedData(List<SeedRoute> routes, List<SeedFlight> flights) {}

    private record SeedRoute(
            String routeName,
            String type,
            String originIata,
            String destinationIata,
            Double estimatedTimeHours,
            Double distanceKm,
            Double requiredRange,
            Integer requiredCapacity
    ) {}

    private record SeedFlight(
            String routeName,
            String aircraftRegistration,
            String scheduledDeparture,
            String scheduledArrival
    ) {}
}
