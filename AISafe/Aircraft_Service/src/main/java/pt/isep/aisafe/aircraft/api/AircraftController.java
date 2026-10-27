package pt.isep.aisafe.aircraft.api;

import pt.isep.aisafe.aircraft.api.dto.AircraftResponse;
import pt.isep.aisafe.aircraft.api.dto.CreateAircraftRequest;
import pt.isep.aisafe.aircraft.api.dto.UpdateStatusRequest;
import pt.isep.aisafe.aircraft.application.AircraftService;
import pt.isep.aisafe.aircraft.domain.Aircraft;
import pt.isep.aisafe.aircraft.domain.AircraftAvailability;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aircraft")
public class AircraftController {

    private final AircraftService aircraftService;

    public AircraftController(AircraftService aircraftService) {
        this.aircraftService = aircraftService;
    }

    @PostMapping
    public ResponseEntity<AircraftResponse> createAircraft(@RequestBody CreateAircraftRequest request) {
        Aircraft created = aircraftService.registerAircraft(
                request.registrationNumber(),
                request.modelName(),
                request.manufacturingDate(),
                request.totalOperationalHours(),
                request.totalFlightHours()
        );

        if (request.features() != null) {
            request.features().forEach(created::addFeature);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @GetMapping("/{registrationNumber}")
    public ResponseEntity<AircraftResponse> getAircraft(@PathVariable String registrationNumber) {
        Aircraft aircraft = aircraftService.getAircraftByRegistration(registrationNumber);
        return ResponseEntity.ok(toResponse(aircraft));
    }

    @PatchMapping("/{registrationNumber}/status")
    public ResponseEntity<AircraftResponse> updateStatus(
            @PathVariable String registrationNumber,
            @RequestBody UpdateStatusRequest request) {
        Aircraft updated = aircraftService.updateStatus(registrationNumber, request.status());
        return ResponseEntity.ok(toResponse(updated));
    }

    @GetMapping
    public ResponseEntity<List<AircraftResponse>> searchAircraft(@RequestParam(required = false) AircraftAvailability status) {
        List<AircraftResponse> list = aircraftService.searchAircraft(status).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    private AircraftResponse toResponse(Aircraft aircraft) {
        return new AircraftResponse(
                aircraft.getRegistrationNumber(),
                aircraft.getModel().getModelName(),
                aircraft.getModel().getManufacturer(),
                aircraft.getStatus(),
                aircraft.getManufacturingDate(),
                aircraft.getTotalOperationalHours(),
                aircraft.getTotalFlightHours(),
                aircraft.getMeanRange(),
                aircraft.getFeatures()
        );
    }
}
