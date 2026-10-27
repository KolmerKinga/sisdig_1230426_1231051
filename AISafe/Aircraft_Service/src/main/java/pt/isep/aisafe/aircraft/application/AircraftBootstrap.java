package pt.isep.aisafe.aircraft.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import pt.isep.aisafe.aircraft.api.dto.CreateAircraftRequest;
import pt.isep.aisafe.aircraft.domain.*;
import pt.isep.aisafe.aircraft.infrastructure.AircraftModelRepository;
import pt.isep.aisafe.aircraft.infrastructure.AircraftRepository;

import java.io.InputStream;
import java.util.List;

@Component
public class AircraftBootstrap implements CommandLineRunner {

    private final AircraftRepository aircraftRepository;
    private final AircraftModelRepository modelRepository;
    private final AircraftService aircraftService;

    @Value("${aisafe.seed-file:#{null}}")
    private Resource seedFile;

    public AircraftBootstrap(AircraftRepository aircraftRepository,
                             AircraftModelRepository modelRepository,
                             AircraftService aircraftService) {
        this.aircraftRepository = aircraftRepository;
        this.modelRepository = modelRepository;
        this.aircraftService = aircraftService;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Criar Modelos Base no Catálogo (se não existirem)
        if (modelRepository.count() == 0) {
            AircraftSpecs specsA320 = new AircraftSpecs(24210.0, 6100.0, 840.0, 180);
            AircraftModel a320 = new AircraftModel("A320-200", "Airbus", specsA320, List.of("https://example.com/a320.jpg"));

            AircraftSpecs specsB737 = new AircraftSpecs(26020.0, 5765.0, 842.0, 189);
            AircraftModel b737 = new AircraftModel("B737-800", "Boeing", specsB737, List.of("https://example.com/b737.jpg"));

            modelRepository.saveAll(List.of(a320, b737));
        }

        // 2. Carregar aeronaves a partir do ficheiro JSON da instância (se configurado)
        if (seedFile != null && seedFile.exists() && aircraftRepository.count() == 0) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            try (InputStream inputStream = seedFile.getInputStream()) {
                List<CreateAircraftRequest> requests = mapper.readValue(
                        inputStream,
                        new TypeReference<List<CreateAircraftRequest>>() {}
                );

                for (CreateAircraftRequest req : requests) {
                    aircraftService.registerAircraft(
                            req.registrationNumber(),
                            req.modelName(),
                            req.manufacturingDate(),
                            req.totalOperationalHours(),
                            req.totalFlightHours()
                    );
                }
            }
        }
    }
}
