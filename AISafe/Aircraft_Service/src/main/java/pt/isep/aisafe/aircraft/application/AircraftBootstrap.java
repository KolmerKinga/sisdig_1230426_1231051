package pt.isep.aisafe.aircraft.application;

import pt.isep.aisafe.aircraft.domain.*;
import pt.isep.aisafe.aircraft.infrastructure.AircraftModelRepository;
import pt.isep.aisafe.aircraft.infrastructure.AircraftRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class AircraftBootstrap implements CommandLineRunner {

    private final AircraftRepository aircraftRepository;
    private final AircraftModelRepository modelRepository;

    public AircraftBootstrap(AircraftRepository aircraftRepository, AircraftModelRepository modelRepository) {
        this.aircraftRepository = aircraftRepository;
        this.modelRepository = modelRepository;
    }

    @Override
    public void run(String... args) {
        if (modelRepository.count() == 0) {
            // Modelo 1: Airbus A320-200
            AircraftSpecs specsA320 = new AircraftSpecs(24210.0, 6100.0, 840.0, 180);
            AircraftModel a320 = new AircraftModel(
                    "A320-200",
                    "Airbus",
                    specsA320,
                    List.of("https://example.com/images/a320.jpg")
            );

            // Modelo 2: Boeing 737-800
            AircraftSpecs specsB737 = new AircraftSpecs(26020.0, 5765.0, 842.0, 189);
            AircraftModel b737 = new AircraftModel(
                    "B111-100",
                    "Boeing",
                    specsB737,
                    List.of("https://example.com/images/b737.jpg")
            );

            modelRepository.saveAll(List.of(a320, b737));

            if (aircraftRepository.count() == 0) {
                // Aeronave 1
                Aircraft aircraft1 = new Aircraft("CS-TMA", a320, LocalDate.of(2021, 3, 10), 1200.0, 950.0);
                aircraft1.activateAircraft();
                aircraft1.addFeature("Wi-Fi");
                aircraft1.addFeature("Extra Legroom");
                aircraft1.configureSeating(new SeatingPack(180, "3-3"));

                // Aeronave 2
                Aircraft aircraft2 = new Aircraft("CS-TMB", b737, LocalDate.of(2019, 7, 22), 3400.0, 2800.0);
                aircraft2.activateAircraft();
                aircraft2.addFeature("USB Charging");
                aircraft2.configureSeating(new SeatingPack(189, "3-3"));

                aircraftRepository.saveAll(List.of(aircraft1, aircraft2));
            }
        }
    }
}
