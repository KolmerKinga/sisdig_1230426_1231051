package pt.isep.aisafe.airport.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import pt.isep.aisafe.airport.api.dto.CreateAirportRequest;

import java.io.InputStream;
import java.util.List;

/**
 * Loads the seed airports of THIS replica (aisafe.bootstrap.file).
 * Each replica gets a different set, so the demo shows data partitioning and forwarding:
 * instance1 holds OPO, LIS, MAD; instance2 holds CDG, LHR, FAO.
 */
@Component
public class AirportBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AirportBootstrap.class);

    private final AirportService airportService;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final String seedFile;

    public AirportBootstrap(AirportService airportService, ObjectMapper objectMapper, ResourceLoader resourceLoader,
                            @Value("${aisafe.bootstrap.file:}") String seedFile) {
        this.airportService = airportService;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.seedFile = seedFile;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (seedFile == null || seedFile.isBlank() || airportService.countLocal() > 0) {
            return;
        }
        Resource resource = resourceLoader.getResource(seedFile);
        if (!resource.exists()) {
            log.warn("Seed file {} not found; starting with an empty store", seedFile);
            return;
        }
        try (InputStream input = resource.getInputStream()) {
            List<CreateAirportRequest> seed = objectMapper.readValue(input,
                    new TypeReference<List<CreateAirportRequest>>() {
                    });
            seed.forEach(request -> airportService.register(request.toDomain()));
            log.info("Bootstrap: {} airport(s) loaded from {}", seed.size(), seedFile);
        }
    }
}
