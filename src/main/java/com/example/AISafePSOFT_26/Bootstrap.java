package com.example.AISafePSOFT_26;

import com.example.AISafePSOFT_26.Aircraft.domain.*;
import com.example.AISafePSOFT_26.Aircraft.infrastructure.AircraftRepository;
import com.example.AISafePSOFT_26.AircraftCatalog.domain.AircraftModel;
import com.example.AISafePSOFT_26.AircraftCatalog.domain.AircraftSpecs;
import com.example.AISafePSOFT_26.AircraftCatalog.infrastructure.AircraftModelRepository;
import com.example.AISafePSOFT_26.Airport.domain.*;
import com.example.AISafePSOFT_26.Airport.infrastructure.AirportRepository;
import com.example.AISafePSOFT_26.Flight.domain.Flight;
import com.example.AISafePSOFT_26.Flight.infrastructure.FlightRepository;
import com.example.AISafePSOFT_26.Maintenance.domain.*;
import com.example.AISafePSOFT_26.Maintenance.infrastructure.MaintenanceRecordRepository;
import com.example.AISafePSOFT_26.Maintenance.infrastructure.MaintenanceTemplateRepository;
import com.example.AISafePSOFT_26.Route.domain.*;
import com.example.AISafePSOFT_26.Route.infrastructure.RouteRepository;
import com.example.AISafePSOFT_26.Users.domain.Collaborator;
import com.example.AISafePSOFT_26.Users.domain.Role;
import com.example.AISafePSOFT_26.Users.infrastructure.CollaboratorRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Profile("dev")
public class Bootstrap implements ApplicationRunner {

    private final CollaboratorRepository collaboratorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AircraftModelRepository aircraftModelRepository;
    private final AirportRepository airportRepository;
    private final AircraftRepository aircraftRepository;
    private final FlightRepository flightRepository;
    private final RouteRepository routeRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final MaintenanceTemplateRepository maintenanceTemplateRepository;

    public Bootstrap(CollaboratorRepository collaboratorRepository, PasswordEncoder passwordEncoder,
            AircraftModelRepository aircraftModelRepository, AirportRepository airportRepository,
            AircraftRepository aircraftRepository, FlightRepository flightRepository,
            RouteRepository routeRepository, MaintenanceRecordRepository maintenanceRecordRepository,
            MaintenanceTemplateRepository maintenanceTemplateRepository) {
        this.collaboratorRepository = collaboratorRepository;
        this.passwordEncoder = passwordEncoder;
        this.aircraftModelRepository = aircraftModelRepository;
        this.airportRepository = airportRepository;
        this.aircraftRepository = aircraftRepository;
        this.flightRepository = flightRepository;
        this.routeRepository = routeRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
        this.maintenanceTemplateRepository = maintenanceTemplateRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (collaboratorRepository.count() == 0) {
            seedCollaborators();
        }

        if (aircraftModelRepository.count() == 0) {
            seedAircraftModels();
        }

        if (airportRepository.count() == 0) {
            seedAirports();
        }

        if (aircraftRepository.count() == 0) {
            seedAircraft();
        }

        if (routeRepository.count() == 0) {
            seedRoutes();
        }

        if (flightRepository.count() == 0) {
            seedFlights();
        }

        if (maintenanceRecordRepository.count() == 0) {
            seedMaintenance();
        }
    }

    private void seedCollaborators() {
        collaboratorRepository.saveAll(List.of(
                new Collaborator("admin", "admin@email.com",
                        passwordEncoder.encode("admin123"), Set.of(Role.ADMIN)),

                new Collaborator("atcc", "atcc@email.com",
                        passwordEncoder.encode("atcc123"), Set.of(Role.ATCC)),

                new Collaborator("backoffice", "backoffice@email.com",
                        passwordEncoder.encode("backoffice123"), Set.of(Role.BACKOFFICE)),

                new Collaborator("technician", "technician@email.com",
                        passwordEncoder.encode("technician123"), Set.of(Role.MAINTENANCE_TECHNICIAN)),

                new Collaborator("supervisor", "supervisor@email.com",
                        passwordEncoder.encode("supervisor123"), Set.of(Role.MAINTENANCE_SUPERVISOR))
        ));
    }

    private void seedAircraftModels() {
        aircraftModelRepository.saveAll(List.of(
                new AircraftModel(
                        "737-800",
                        "Boeing",
                        new AircraftSpecs(70500.0, 5436.0, 842.0, 189),
                        List.of("b737-800-front.jpg", "b737-800-side.jpg", "b737-800-cabin.jpg")
                ),

                new AircraftModel(
                        "737 MAX 8",
                        "Boeing",
                        new AircraftSpecs(82000.0, 6570.0, 839.0, 178),
                        List.of("b737-max8-1.jpg", "b737-max8-2.jpg")
                ),

                new AircraftModel(
                        "A320neo",
                        "Airbus",
                        new AircraftSpecs(79000.0, 6300.0, 828.0, 180),
                        List.of("a320neo-1.jpg", "a320neo-2.jpg", "a320neo-cockpit.jpg")
                ),

                new AircraftModel(
                        "A321neo",
                        "Airbus",
                        new AircraftSpecs(97000.0, 7400.0, 828.0, 220),
                        List.of("a321neo-1.jpg", "a321neo-2.jpg")
                ),

                new AircraftModel(
                        "A350-900",
                        "Airbus",
                        new AircraftSpecs(280000.0, 15000.0, 903.0, 300),
                        List.of("a350-900-1.jpg", "a350-900-2.jpg", "a350-interior.jpg")
                ),

                new AircraftModel(
                        "A380-800",
                        "Airbus",
                        new AircraftSpecs(575000.0, 15200.0, 903.0, 500),
                        List.of("a380-1.jpg", "a380-2.jpg", "a380-double-deck.jpg")
                ),

                new AircraftModel(
                        "787-9 Dreamliner",
                        "Boeing",
                        new AircraftSpecs(254000.0, 14140.0, 913.0, 296),
                        List.of("b787-9-1.jpg", "b787-9-cabin.jpg")
                ),

                new AircraftModel(
                        "787-10 Dreamliner",
                        "Boeing",
                        new AircraftSpecs(254000.0, 11800.0, 903.0, 330),
                        List.of("b787-10-1.jpg", "b787-10-2.jpg")
                ),

                new AircraftModel(
                        "777-300ER",
                        "Boeing",
                        new AircraftSpecs(351500.0, 13600.0, 905.0, 396),
                        List.of("b777-300er-1.jpg", "b777-300er-cabin.jpg")
                ),

                new AircraftModel(
                        "E195-E2",
                        "Embraer",
                        new AircraftSpecs(61500.0, 4815.0, 870.0, 132),
                        List.of("e195-e2-1.jpg", "e195-e2-2.jpg")
                ),

                new AircraftModel(
                        "E190-E2",
                        "Embraer",
                        new AircraftSpecs(56700.0, 4537.0, 870.0, 114),
                        List.of("e190-e2-1.jpg", "e190-e2-cabin.jpg")
                ),

                new AircraftModel(
                        "CRJ900",
                        "Bombardier",
                        new AircraftSpecs(34000.0, 2871.0, 880.0, 90),
                        List.of("crj900-1.jpg", "crj900-2.jpg")
                ),

                new AircraftModel(
                        "ATR 72-600",
                        "ATR",
                        new AircraftSpecs(23000.0, 1528.0, 510.0, 78),
                        List.of("atr72-1.jpg", "atr72-cabin.jpg")
                )
        ));
    }

    private void seedAirports() {
        airportRepository.save(new Airport(
                "OPO",
                "International",
                "Francisco Sá Carneiro Airport",
                AirportStatus.OPERATIONAL,
                new AirportLocation(
                        "Porto",
                        -8.68139,
                        41.2421,
                        "Porto District",
                        "Europe/Lisbon",
                        "Portugal"
                ),
                new Facilities(
                        1,
                        List.of(
                                "Cargo Terminal",
                                "Restaurants",
                                "Duty Free",
                                "Free WiFi",
                                "Parking"
                        ),
                        35
                ),
                313.0,
                List.of(
                        new Runway(
                                null,
                                "17/35",
                                3480.0,
                                "Asphalt",
                                RunwayStatus.IN_USE
                        )
                ),
                List.of(
                        new Certification(
                                "ICAO Annex 14 Compliance",
                                "SAFETY",
                                LocalDate.of(2023, 1, 1),
                                LocalDate.of(2027, 1, 1)
                        )
                ),
                List.of(
                        new Contact(ContactType.PHONE, "+351 22 943 2400"),
                        new Contact(ContactType.EMAIL, "info@ana.pt")
                ),
                List.of(
                        "opo_terminal.jpg",
                        "opo_runway.jpg",
                        "opo_night_view.jpg"
                )
        ));

        airportRepository.save(new Airport(
                "LIS",
                "International",
                "Humberto Delgado Airport",
                AirportStatus.OPERATIONAL,
                new AirportLocation(
                        "Lisbon",
                        -9.13592,
                        38.7813,
                        "Lisbon Region",
                        "Europe/Lisbon",
                        "Portugal"
                ),
                new Facilities(
                        2,
                        List.of(
                                "VIP Lounge",
                                "Medical Center",
                                "Duty Free",
                                "Metro Access",
                                "WiFi",
                                "Conference Rooms"
                        ),
                        47
                ),
                0.0,
                List.of(
                        new Runway(
                                null,
                                "03/21",
                                3805.0,
                                "Asphalt",
                                RunwayStatus.OPEN
                        ),
                        new Runway(
                                null,
                                "17/35",
                                2400.0,
                                "Asphalt",
                                RunwayStatus.UNDER_MAINTENANCE
                        )
                ),
                List.of(
                        new Certification(
                                "ICAO Safety Certification",
                                "SAFETY",
                                LocalDate.of(2024, 1, 1),
                                LocalDate.of(2028, 1, 1)
                        ),
                        new Certification(
                                "EASA Aerodrome License",
                                "REGULATORY",
                                LocalDate.of(2023, 6, 1),
                                LocalDate.of(2026, 6, 1)
                        )
                ),
                List.of(
                        new Contact(ContactType.PHONE, "+351 21 841 3500"),
                        new Contact(ContactType.EMAIL, "lisbon.airport@ana.pt")
                ),
                List.of(
                        "lis_terminal_1.jpg",
                        "lis_terminal_2.jpg",
                        "lis_runway_night.jpg"
                )
        ));

        airportRepository.save(new Airport(
                "MAD",
                "International",
                "Adolfo Suárez Madrid–Barajas Airport",
                AirportStatus.OPERATIONAL,
                new AirportLocation(
                        "Madrid",
                        -3.56795,
                        40.4983,
                        "Madrid Region",
                        "Europe/Madrid",
                        "Spain"
                ),
                new Facilities(
                        4,
                        List.of(
                                "High-Speed Rail Connection",
                                "VIP Lounges",
                                "Shopping Mall",
                                "Cargo Hub",
                                "Hotel On-Site",
                                "WiFi"
                        ),
                        60
                ),
                0.0,
                List.of(
                        new Runway(
                                null,
                                "14L/32R",
                                3500.0,
                                "Asphalt",
                                RunwayStatus.IN_USE
                        ),
                        new Runway(
                                null,
                                "14R/32L",
                                3500.0,
                                "Asphalt",
                                RunwayStatus.IN_USE
                        ),
                        new Runway(
                                null,
                                "18L/36R",
                                4100.0,
                                "Asphalt",
                                RunwayStatus.OPEN
                        ),
                        new Runway(
                                null,
                                "18R/36L",
                                3900.0,
                                "Asphalt",
                                RunwayStatus.OPEN
                        )
                ),
                List.of(
                        new Certification(
                                "EU Major Airport Compliance",
                                "REGULATORY",
                                LocalDate.of(2022, 5, 1),
                                LocalDate.of(2027, 5, 1)
                        )
                ),
                List.of(
                        new Contact(ContactType.PHONE, "+34 913 21 10 00"),
                        new Contact(ContactType.EMAIL, "info@aena.es")
                ),
                List.of(
                        "mad_terminal.jpg",
                        "mad_runways.jpg",
                        "mad_control_tower.jpg"
                )
        ));

        airportRepository.save(new Airport(
                "CDG",
                "International",
                "Charles de Gaulle Airport",
                AirportStatus.OPERATIONAL,
                new AirportLocation(
                        "Paris",
                        2.55,
                        49.0097,
                        "Île-de-France",
                        "Europe/Paris",
                        "France"
                ),
                new Facilities(
                        3,
                        List.of(
                                "TGV Station",
                                "Luxury Lounges",
                                "Cargo Hub",
                                "Duty Free",
                                "WiFi",
                                "Medical Center"
                        ),
                        78
                ),
                0.0,
                List.of(
                        new Runway(
                                null,
                                "09L/27R",
                                4200.0,
                                "Concrete",
                                RunwayStatus.IN_USE
                        ),
                        new Runway(
                                null,
                                "09R/27L",
                                2700.0,
                                "Concrete",
                                RunwayStatus.OPEN
                        ),
                        new Runway(
                                null,
                                "08L/26R",
                                4215.0,
                                "Concrete",
                                RunwayStatus.OPEN
                        )
                ),
                List.of(
                        new Certification(
                                "ICAO Category III Landing Capability",
                                "SAFETY",
                                LocalDate.of(2023, 9, 1),
                                LocalDate.of(2028, 9, 1)
                        )
                ),
                List.of(
                        new Contact(ContactType.PHONE, "+33 1 70 36 39 50"),
                        new Contact(ContactType.EMAIL, "info@parisaeroport.fr")
                ),
                List.of(
                        "cdg_terminal.jpg",
                        "cdg_runway.jpg",
                        "cdg_night.jpg"
                )
        ));

        airportRepository.save(new Airport(
                "LHR",
                "International",
                "London Heathrow Airport",
                AirportStatus.OPERATIONAL,
                new AirportLocation(
                        "London",
                        -0.4543,
                        51.4700,
                        "Greater London",
                        "Europe/London",
                        "United Kingdom"
                ),
                new Facilities(
                        4,
                        List.of(
                                "Heathrow Express",
                                "Premium Lounges",
                                "Duty Free",
                                "Hotels",
                                "WiFi",
                                "Cargo Operations"
                        ),
                        85
                ),
                0.0,
                List.of(
                        new Runway(
                                null,
                                "09L/27R",
                                3902.0,
                                "Asphalt",
                                RunwayStatus.IN_USE
                        ),
                        new Runway(
                                null,
                                "09R/27L",
                                3660.0,
                                "Asphalt",
                                RunwayStatus.OPEN
                        )
                ),
                List.of(
                        new Certification(
                                "UK CAA Operational Approval",
                                "REGULATORY",
                                LocalDate.of(2024, 2, 1),
                                LocalDate.of(2029, 2, 1)
                        )
                ),
                List.of(
                        new Contact(ContactType.PHONE, "+44 844 335 1801"),
                        new Contact(ContactType.EMAIL, "info@heathrow.com")
                ),
                List.of(
                        "lhr_terminal.jpg",
                        "lhr_runway.jpg"
                )
        ));
    }

    private void seedAircraft() {
        AircraftModel b737 = aircraftModelRepository
                .findByModelName("737-800")
                .orElseThrow();

        Aircraft a1 = new Aircraft("CS-TUA", b737,
                LocalDate.of(2018, 5, 10),
                12500.0,
                9800.0);

        a1.configureSeating(new SeatingPack(189, "3-3"));
        a1.addFeature("Wi-Fi");
        a1.addFeature("USB Charging");
        a1.addCertification(new AircraftCertification(
                "EASA Safety Compliance Approval",
                "SAFETY",
                LocalDate.of(2023, 6, 1),
                LocalDate.of(2026, 6, 1)
        ));
        a1.installComponent(new InstalledComponent(
                "CFM56-7B-001",
                "CFM56-7B",
                "ACTIVE",
                a1
        ));
        a1.retireAircraft();

        Aircraft a2 = new Aircraft("D-ABXA", b737,
                LocalDate.of(2016, 2, 18),
                18500.0,
                14200.0);

        a2.configureSeating(new SeatingPack(189, "3-3"));
        a2.addFeature("Wi-Fi");
        a2.addCertification(new AircraftCertification(
                "ICAO Operational Certification",
                "OPERATIONAL",
                LocalDate.of(2021, 1, 10),
                LocalDate.of(2024, 1, 10)
        ));
        a2.installComponent(new InstalledComponent(
                "CFM56-7B-002",
                "CFM56-7B",
                "ACTIVE",
                a2
        ));
        a2.activateAircraft();

        AircraftModel a320 = aircraftModelRepository
                .findByModelName("A320neo")
                .orElseThrow();

        Aircraft medium = new Aircraft(
                "TEST-002",
                a320,
                LocalDate.of(2022, 1, 1),
                1000.0,
                800.0
        );

        medium.configureSeating(new SeatingPack(180, "3-3"));

        Aircraft a3 = new Aircraft("F-HXKA", a320,
                LocalDate.of(2020, 9, 5),
                8200.0,
                6100.0);

        a3.configureSeating(new SeatingPack(180, "3-3"));
        a3.addFeature("Wi-Fi");
        a3.addFeature("In-flight Entertainment");
        a3.addCertification(new AircraftCertification(
                "EASA Airworthiness Certificate",
                "SAFETY",
                LocalDate.of(2022, 4, 12),
                LocalDate.of(2025, 4, 12)
        ));
        a3.installComponent(new InstalledComponent(
                "LEAP-1A-001",
                "LEAP-1A",
                "ACTIVE",
                a3
        ));
        a3.sendToMaintenance();

        AircraftModel a321 = aircraftModelRepository
                .findByModelName("A321neo")
                .orElseThrow();

        Aircraft a4 = new Aircraft("G-X321", a321,
                LocalDate.of(2021, 1, 20),
                5400.0,
                4100.0);

        a4.configureSeating(new SeatingPack(220, "3-3"));
        a4.addFeature("WiFi");
        a4.addFeature("Extra Legroom Rows");
        a4.installComponent(new InstalledComponent(
                "PW1100G-001",
                "PW1100G",
                "ACTIVE",
                a4
        ));

        AircraftModel b787 = aircraftModelRepository
                .findByModelName("787-9 Dreamliner")
                .orElseThrow();

        Aircraft large = new Aircraft(
                "TEST-003",
                b787,
                LocalDate.of(2022, 1, 1),
                1000.0,
                800.0
        );

        large.configureSeating(new SeatingPack(300, "3-3-3"));

        Aircraft a5 = new Aircraft("N789DL", b787,
                LocalDate.of(2019, 7, 30),
                6000.0,
                4500.0);

        a5.configureSeating(new SeatingPack(296, "3-3-3"));
        a5.addFeature("Lie-flat Business Class");
        a5.addFeature("WiFi");
        a5.addCertification(new AircraftCertification(
                "FAA Long-haul Certification",
                "OPERATIONAL",
                LocalDate.of(2020, 5, 1),
                LocalDate.of(2025, 5, 1)
        ));
        a5.installComponent(new InstalledComponent(
                "GEnx-1B-001",
                "GEnx-1B",
                "ACTIVE",
                a5
        ));

        AircraftModel e195 = aircraftModelRepository
                .findByModelName("E195-E2")
                .orElseThrow();

        Aircraft small = new Aircraft(
                "TEST-001",
                e195,
                LocalDate.of(2022, 1, 1),
                1000.0,
                800.0
        );

        Aircraft a6 = new Aircraft("PR-EMJ", e195,
                LocalDate.of(2022, 3, 11),
                3200.0,
                2100.0);

        a6.configureSeating(new SeatingPack(132, "2-2"));
        a6.addFeature("Wi-Fi");
        a6.addFeature("USB Charging");
        a6.installComponent(new InstalledComponent(
                "PW1900G-001",
                "PW1900G",
                "ACTIVE",
                a6
        ));
        a6.sendAircraftInFlight();
        aircraftRepository.saveAll(List.of(a1, a2, a3, a4, a5, a6,small,medium,large));
    }

    private void seedRoutes() {
        Airport opo = airportRepository.findByIataCode("OPO").orElseThrow();
        Airport lis = airportRepository.findByIataCode("LIS").orElseThrow();
        Airport mad = airportRepository.findByIataCode("MAD").orElseThrow();
        Airport cdg = airportRepository.findByIataCode("CDG").orElseThrow();
        Airport lhr = airportRepository.findByIataCode("LHR").orElseThrow();

        routeRepository.save(new Route(
                new RouteRequirements(1000.0, 100),
                RouteStatus.ACTIVE,
                RouteType.DIRECT,
                new RouteHistory(LocalDate.now(), 0),
                1.0,
                opo,
                lis,
                "TEST ROUTE 1"
        ));

        routeRepository.save(new Route(
                new RouteRequirements(1000.0, 150),
                RouteStatus.ACTIVE,
                RouteType.DIRECT,
                new RouteHistory(LocalDate.now(), 0),
                1.0,
                lis,
                mad,
                "TEST ROUTE 2"
        ));

        routeRepository.save(new Route(
                new RouteRequirements(1000.0, 250),
                RouteStatus.ACTIVE,
                RouteType.DIRECT,
                new RouteHistory(LocalDate.now(), 0),
                1.0,
                mad,
                cdg,
                "TEST ROUTE 3"
        ));

        routeRepository.save(new Route(
                new RouteRequirements(10000.0, 100),
                RouteStatus.ACTIVE,
                RouteType.DIRECT,
                new RouteHistory(LocalDate.now(), 0),
                1.0,
                cdg,
                lhr,
                "TEST ROUTE 4"
        ));

        // ===================== OPO → LIS =====================
        routeRepository.save(new Route(
                new RouteRequirements(2000.0, 128),
                RouteStatus.ACTIVE,
                RouteType.SCALED,
                new RouteHistory(LocalDate.of(2018, 5, 10), 0),
                24.0,200.0,
                opo,
                lis,
                "Pati&Patatá"
        ));

        // ===================== OPO → MAD =====================
        routeRepository.save(new Route(
                new RouteRequirements(8500.0, 180),
                RouteStatus.ACTIVE,
                RouteType.DIRECT,
                new RouteHistory(LocalDate.of(2019, 3, 15), 2),
                1.5,500.0,
                opo,
                mad,
                "Iberia Connect"
        ));

        // ===================== LIS → MAD =====================
        routeRepository.save(new Route(
                new RouteRequirements(9000.0, 180),
                RouteStatus.ACTIVE,
                RouteType.DIRECT,
                new RouteHistory(LocalDate.of(2020, 7, 1), 1),
                1.2,600.0,
                lis,
                mad,
                "TAP Express"
        ));

        // ===================== MAD → CDG =====================
        routeRepository.save(new Route(
                new RouteRequirements(14000.0, 220),
                RouteStatus.ACTIVE,
                RouteType.DIRECT,
                new RouteHistory(LocalDate.of(2017, 10, 20), 5),
                2.1,1000.0,
                mad,
                cdg,
                "Air France Iberia Codeshare"
        ));

        // ===================== CDG → LHR =====================
        routeRepository.save(new Route(
                new RouteRequirements(12000.0, 200),
                RouteStatus.ACTIVE,
                RouteType.DIRECT,
                new RouteHistory(LocalDate.of(2016, 6, 5), 8),
                1.3,700.0,
                cdg,
                lhr,
                "Air France - British Airways Alliance"
        ));

        // ===================== LHR → LIS =====================
        routeRepository.save(new Route(
                new RouteRequirements(15000.0, 240),
                RouteStatus.ACTIVE,
                RouteType.DIRECT,
                new RouteHistory(LocalDate.of(2015, 2, 12), 10),
                2.5,900.0,
                lhr,
                lis,
                "British Airways"
        ));

        // ===================== MAD → OPO =====================
        routeRepository.save(new Route(
                new RouteRequirements(8000.0, 160),
                RouteStatus.ACTIVE,
                RouteType.DIRECT,
                new RouteHistory(LocalDate.of(2021, 11, 30), 1),
                1.4,600.0,
                mad,
                opo,
                "Iberia Regional"
        ));

        // ===================== CDG → MAD =====================
        routeRepository.save(new Route(
                new RouteRequirements(14500.0, 230),
                RouteStatus.ACTIVE,
                RouteType.DIRECT,
                new RouteHistory(LocalDate.of(2018, 8, 25), 6),
                2.0,700.0,
                cdg,
                mad,
                "Air France"
        ));

        // ===================== LHR → CDG =====================
        routeRepository.save(new Route(
                new RouteRequirements(11000.0, 210),
                RouteStatus.ACTIVE,
                RouteType.DIRECT,
                new RouteHistory(LocalDate.of(2017, 4, 14), 7),
                1.25,800.0,
                lhr,
                cdg,
                "British Airways"
        ));
    }

    private void seedFlights() {
        Aircraft csTua = aircraftRepository
                .findByRegistrationNumber("CS-TUA")
                .orElseThrow();

        Aircraft dAbxa = aircraftRepository
                .findByRegistrationNumber("D-ABXA")
                .orElseThrow();

        Aircraft fHxka = aircraftRepository
                .findByRegistrationNumber("F-HXKA")
                .orElseThrow();

        Aircraft n789dl = aircraftRepository
                .findByRegistrationNumber("N789DL")
                .orElseThrow();

        List<Route> routes = routeRepository.findAll();

        Flight f1 = new Flight(
                routes.stream()
                        .filter(r -> r.getRouteName().equals("Pati&Patatá"))
                        .findFirst()
                        .orElseThrow(),
                csTua,
                LocalDateTime.of(2026, 6, 15, 8, 0),
                LocalDateTime.of(2026, 6, 15, 9, 0)
        );


        Flight f2 = new Flight(
                routes.stream()
                        .filter(r -> r.getRouteName().equals("Iberia Connect"))
                        .findFirst()
                        .orElseThrow(),
                dAbxa,
                LocalDateTime.of(2026, 6, 15, 10, 30),
                LocalDateTime.of(2026, 6, 15, 12, 0)
        );


        Flight f3 = new Flight(
                routes.stream()
                        .filter(r -> r.getRouteName().equals("TAP Express"))
                        .findFirst()
                        .orElseThrow(),
                fHxka,
                LocalDateTime.of(2026, 6, 15, 14, 0),
                LocalDateTime.of(2026, 6, 15, 15, 20)
        );


        Flight f4 = new Flight(
                routes.stream()
                        .filter(r -> r.getRouteName().equals("Air France Iberia Codeshare"))
                        .findFirst()
                        .orElseThrow(),
                fHxka,
                LocalDateTime.of(2026, 6, 16, 9, 15),
                LocalDateTime.of(2026, 6, 16, 11, 20)
        );


        Flight f5 = new Flight(
                routes.stream()
                        .filter(r -> r.getRouteName().equals("Air France - British Airways Alliance"))
                        .findFirst()
                        .orElseThrow(),
                csTua,
                LocalDateTime.of(2026, 6, 16, 13, 0),
                LocalDateTime.of(2026, 6, 16, 14, 15)
        );


        Flight f6 = new Flight(
                routes.stream()
                        .filter(r -> r.getRouteName().equals("British Airways"))
                        .findFirst()
                        .orElseThrow(),
                n789dl,
                LocalDateTime.of(2026, 6, 17, 11, 0),
                LocalDateTime.of(2026, 6, 17, 13, 40)
        );


        Flight f7 = new Flight(
                routes.stream()
                        .filter(r -> r.getRouteName().equals("Iberia Regional"))
                        .findFirst()
                        .orElseThrow(),
                dAbxa,
                LocalDateTime.of(2025, 7, 10, 8, 0),
                LocalDateTime.of(2025, 7, 10, 9, 30)
        );


        Flight f8 = new Flight(
                routes.stream()
                        .filter(r -> r.getRouteName().equals("British Airways"))
                        .findFirst()
                        .orElseThrow(),
                n789dl,
                LocalDateTime.of(2025, 9, 5, 14, 0),
                LocalDateTime.of(2025, 9, 5, 16, 20)
        );


        Flight f9 = new Flight(
                routes.stream()
                        .filter(r -> r.getRouteName().equals("Air France"))
                        .findFirst()
                        .orElseThrow(),
                csTua,
                LocalDateTime.of(2025, 11, 20, 7, 30),
                LocalDateTime.of(2025, 11, 20, 9, 45)
        );


        Flight f10 = new Flight(
                routes.stream()
                        .filter(r -> r.getRouteName().equals("TAP Express"))
                        .findFirst()
                        .orElseThrow(),
                fHxka,
                LocalDateTime.of(2026, 1, 12, 10, 0),
                LocalDateTime.of(2026, 1, 12, 11, 20)
        );


        Flight f11 = new Flight(
                routes.stream()
                        .filter(r -> r.getRouteName().equals("Air France Iberia Codeshare"))
                        .findFirst()
                        .orElseThrow(),
                fHxka,
                LocalDateTime.of(2026, 2, 18, 13, 0),
                LocalDateTime.of(2026, 2, 18, 15, 10)
        );


        Flight f12 = new Flight(
                routes.stream()
                        .filter(r -> r.getRouteName().equals("Pati&Patatá"))
                        .findFirst()
                        .orElseThrow(),
                csTua,
                LocalDateTime.of(2026, 3, 22, 8, 0),
                LocalDateTime.of(2026, 3, 22, 9, 0)
        );


        Flight f13 = new Flight(
                routes.stream()
                        .filter(r -> r.getRouteName().equals("British Airways"))
                        .findFirst()
                        .orElseThrow(),
                n789dl,
                LocalDateTime.of(2026, 4, 15, 16, 0),
                LocalDateTime.of(2026, 4, 15, 18, 30)
        );


        Flight f14 = new Flight(
                routes.stream()
                        .filter(r -> r.getRouteName().equals("Air France"))
                        .findFirst()
                        .orElseThrow(),
                dAbxa,
                LocalDateTime.of(2026, 5, 25, 9, 0),
                LocalDateTime.of(2026, 5, 25, 11, 0)
        );

        List<Flight> completedFlights = List.of(
                f1,f3,f4,f7,f8,f9,f10,f11,f12,f13,f14
        );

        completedFlights.forEach(f -> {
            f.startFlight();
            f.completeFlight();
        });

        flightRepository.saveAll(
                List.of(
                        f1,f2,f3,f4,f5,f6,
                        f7,f8,f9,f10,f11,f12,f13,f14
                )
        );
    }

    private void seedMaintenance() {
        Collaborator supervisor = collaboratorRepository
                .findByUsername("supervisor")
                .orElseThrow();

        Collaborator tech = collaboratorRepository
                .findByUsername("technician")
                .orElseThrow();

        Aircraft csTua = aircraftRepository
                .findByRegistrationNumber("CS-TUA")
                .orElseThrow();

        Aircraft dAbxa = aircraftRepository
                .findByRegistrationNumber("D-ABXA")
                .orElseThrow();

        Aircraft fHxka = aircraftRepository
                .findByRegistrationNumber("F-HXKA")
                .orElseThrow();

        // ================= ENGINE INSPECTION =================

        MaintenanceTemplate engineInspection = new MaintenanceTemplate(
                "ENGINE-INSPECTION",
                20.0,
                Map.of(
                        "Check oil", true,
                        "Inspect motor", true,
                        "Verify pressure systems", true
                ),
                MaintenanceType.INSPECTION,
                MaintenanceAttribute.ENGINE
        );

        maintenanceTemplateRepository.save(engineInspection);

        MaintenanceRecord r1 = new MaintenanceRecord(
                List.of(
                        new UsedPart("ENG-001", 2, 1500.0)
                ),
                24.0,
                LocalDate.now().minusDays(10),
                supervisor,
                "Completed",
                List.of(tech),
                engineInspection,
                csTua
        );

        r1.markAsCompleted(new DoneList(
                "Engine inspection completed successfully.",
                Map.of(
                        "Check oil", true,
                        "Inspect motor", true,
                        "Verify pressure systems", true
                )),     LocalDate.of(2003, 3, 1));

        // ================= LANDING GEAR =================

        MaintenanceTemplate landingGearInspection = new MaintenanceTemplate(
                "LANDING-GEAR-CHECK",
                12.0,
                Map.of(
                        "Inspect tires", true,
                        "Check brake assemblies", true,
                        "Verify hydraulic actuators", true
                ),
                MaintenanceType.INSPECTION,
                MaintenanceAttribute.AIRFRAME
        );

        maintenanceTemplateRepository.save(landingGearInspection);

        MaintenanceRecord r2 = new MaintenanceRecord(
                List.of(
                        new UsedPart("TIRE-900", 4, 800.0),
                        new UsedPart("BRAKE-200", 2, 1200.0)
                ),
                15.0,
                LocalDate.now().minusDays(5),
                supervisor,
                "Completed",
                List.of(tech),
                landingGearInspection,
                dAbxa
        );
        r2.markAsCompleted(new DoneList(
                "Landing gear inspection completed. Tires replaced.",
                Map.of(
                        "Inspect tires", true,
                        "Check brake assemblies", true,
                        "Verify hydraulic actuators", true
                )
        ),LocalDate.of(2013, 4, 12));

        // ================= AVIONICS =================

        MaintenanceTemplate avionicsCheck = new MaintenanceTemplate(
                "AVIONICS-DIAGNOSTIC",
                8.0,
                Map.of(
                        "Run diagnostics", true,
                        "Inspect flight computer", true,
                        "Verify communication systems", true
                ),
                MaintenanceType.INSPECTION,
                MaintenanceAttribute.AVIONICS
        );

        maintenanceTemplateRepository.save(avionicsCheck);

        MaintenanceRecord r3 = new MaintenanceRecord(
                List.of(),
                8.0,
                LocalDate.now().minusDays(2),
                supervisor,
                "Completed",
                List.of(tech),
                avionicsCheck,
                fHxka
        );

        r3.markAsCompleted(new DoneList(
                "Avionics diagnostic completed with no issues.",
                Map.of(
                        "Run diagnostics", true,
                        "Inspect flight computer", true,
                        "Verify communication systems", true
                )
        ),LocalDate.of(2023, 9, 21));

        // ================= ENGINE REPAIR =================

        MaintenanceTemplate engineRepair = new MaintenanceTemplate(
                "ENGINE-REPAIR",
                40.0,
                Map.of(
                        "Replace fuel pump", true,
                        "Replace oil filter", true,
                        "Ground engine test", true
                ),
                MaintenanceType.OVERHAUL,
                MaintenanceAttribute.ENGINE
        );

        maintenanceTemplateRepository.save(engineRepair);

        MaintenanceRecord r4 = new MaintenanceRecord(
                List.of(
                        new UsedPart("FUEL-PUMP-01", 1, 3200.0),
                        new UsedPart("OIL-FILTER-22", 2, 250.0)
                ),
                40.0,
                LocalDate.now(),
                supervisor,
                "In Progress",
                List.of(tech),
                engineRepair,
                csTua
        );

        maintenanceRecordRepository.saveAll(
                List.of(r1, r2, r3, r4)
        );
    }
}