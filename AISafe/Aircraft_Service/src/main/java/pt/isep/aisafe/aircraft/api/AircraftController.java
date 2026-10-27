package pt.isep.aisafe.aircraft.api;

import pt.isep.aisafe.aircraft.api.dto.AircraftResponse;
import pt.isep.aisafe.aircraft.api.dto.CreateAircraftRequest;
import pt.isep.aisafe.aircraft.api.dto.UpdateStatusRequest;
import pt.isep.aisafe.aircraft.application.AircraftService;
import pt.isep.aisafe.aircraft.domain.Aircraft;
import pt.isep.aisafe.aircraft.domain.AircraftAvailability;
import pt.isep.aisafe.aircraft.shared.forwarding.ForwardingHeaders;
import pt.isep.aisafe.aircraft.shared.forwarding.PeerClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/aircraft")
public class AircraftController {

    private final AircraftService aircraftService;
    private final PeerClient peerClient;

    public AircraftController(AircraftService aircraftService, PeerClient peerClient) {
        this.aircraftService = aircraftService;
        this.peerClient = peerClient;
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
    public ResponseEntity<List<AircraftResponse>> searchAircraft(
            @RequestParam(required = false) AircraftAvailability status,
            @RequestHeader(value = ForwardingHeaders.FORWARDED_FROM, required = false) String forwardedFrom) {

        // 1. Obter aeronaves da base de dados da réplica local
        List<AircraftResponse> localList = aircraftService.searchAircraft(status).stream()
                .map(this::toResponse)
                .toList();

        // 2. Se o pedido veio reencaminhado de outra réplica, devolve apenas a lista local (evita loop infinito)
        if (forwardedFrom != null) {
            return ResponseEntity.ok(localList);
        }

        // 3. Se é o nó inicial que recebeu o pedido do cliente/Postman, agrega os dados locais com os das outras réplicas
        List<AircraftResponse> result = new ArrayList<>(localList);
        List<AircraftResponse[]> remoteResponses = peerClient.fetchFromOtherPeers("/api/aircraft", AircraftResponse[].class);

        for (AircraftResponse[] remoteArray : remoteResponses) {
            if (remoteArray != null) {
                result.addAll(Arrays.asList(remoteArray));
            }
        }

        return ResponseEntity.ok(result);
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
