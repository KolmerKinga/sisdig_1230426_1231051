package pt.isep.aisafe.aircraft.api;

import pt.isep.aisafe.aircraft.api.dto.AircraftModelResponse;
import pt.isep.aisafe.aircraft.api.dto.CreateAircraftModelRequest;
import pt.isep.aisafe.aircraft.application.AircraftService;
import pt.isep.aisafe.aircraft.domain.AircraftModel;
import pt.isep.aisafe.aircraft.domain.AircraftSpecs;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aircraft-models")
public class AircraftModelController {

    private final AircraftService aircraftService;

    public AircraftModelController(AircraftService aircraftService) {
        this.aircraftService = aircraftService;
    }

    @PostMapping
    public ResponseEntity<AircraftModelResponse> createModel(@RequestBody CreateAircraftModelRequest request) {
        AircraftSpecs specs = new AircraftSpecs(
                request.fuelCapacityLiters(),
                request.maximumRangeKm(),
                request.cruisingSpeedKph(),
                request.standardSeatingCapacity()
        );

        AircraftModel model = new AircraftModel(
                request.modelName(),
                request.manufacturer(),
                specs,
                request.modelImages()
        );

        AircraftModel created = aircraftService.registerAircraftModel(model);
        return ResponseEntity.status(HttpStatus.CREATED).body(toModelResponse(created));
    }

    @GetMapping("/{modelName}")
    public ResponseEntity<AircraftModelResponse> getModel(@PathVariable String modelName) {
        AircraftModel model = aircraftService.getAircraftModelByName(modelName);
        return ResponseEntity.ok(toModelResponse(model));
    }

    @GetMapping
    public ResponseEntity<List<AircraftModelResponse>> getAllModels() {
        List<AircraftModelResponse> models = aircraftService.getAllAircraftModels().stream()
                .map(this::toModelResponse)
                .toList();
        return ResponseEntity.ok(models);
    }

    private AircraftModelResponse toModelResponse(AircraftModel model) {
        AircraftSpecs specs = model.getAircraftModelSpecs();
        return new AircraftModelResponse(
                model.getModelName(),
                model.getManufacturer(),
                specs.getFuelCapacity(),
                specs.getMaximumRange(),
                specs.getCruisingSpeed(),
                specs.getStandardSeatingCapacity(),
                model.getModelImages()
        );
    }
}
