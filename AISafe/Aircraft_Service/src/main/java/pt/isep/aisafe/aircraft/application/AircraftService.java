package pt.isep.aisafe.aircraft.application;

import pt.isep.aisafe.aircraft.domain.*;
import pt.isep.aisafe.aircraft.infrastructure.AircraftModelRepository;
import pt.isep.aisafe.aircraft.infrastructure.AircraftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AircraftService {

    private final AircraftRepository aircraftRepository;
    private final AircraftModelRepository modelRepository;

    public AircraftService(AircraftRepository aircraftRepository, AircraftModelRepository modelRepository) {
        this.aircraftRepository = aircraftRepository;
        this.modelRepository = modelRepository;
    }

    // --- Casos de Uso: Aircraft ---

    @Transactional
    public Aircraft registerAircraft(String regNumber, String modelName, LocalDate mfgDate, Double opHours, Double flightHours) {
        if (aircraftRepository.existsByRegistrationNumber(regNumber)) {
            throw new AircraftAlreadyExistsException(regNumber);
        }

        AircraftModel model = modelRepository.findById(modelName)
                .orElseThrow(() -> new AircraftModelNotFoundException(modelName));

        Aircraft aircraft = new Aircraft(regNumber, model, mfgDate, opHours, flightHours);
        return aircraftRepository.save(aircraft);
    }

    @Transactional(readOnly = true)
    public Aircraft getAircraftByRegistration(String regNumber) {
        return aircraftRepository.findByRegistrationNumber(regNumber)
                .orElseThrow(() -> new AircraftNotFoundException(regNumber));
    }

    @Transactional
    public Aircraft updateStatus(String regNumber, AircraftAvailability newStatus) {
        if (newStatus == null) {
            throw new InvalidRequestException("Status cannot be null");
        }

        Aircraft aircraft = getAircraftByRegistration(regNumber);
        switch (newStatus) {
            case AVAILABLE -> aircraft.activateAircraft();
            case MAINTENANCE -> aircraft.sendToMaintenance();
            case INACTIVE -> aircraft.retireAircraft();
            case IN_FLIGHT -> aircraft.sendAircraftInFlight();
        }

        return aircraftRepository.save(aircraft);
    }

    @Transactional(readOnly = true)
    public List<Aircraft> searchAircraft(AircraftAvailability status) {
        if (status != null) {
            return aircraftRepository.findByStatus(status);
        }
        return aircraftRepository.findAll();
    }

    // --- Casos de Uso: AircraftModel ---

    @Transactional
    public AircraftModel registerAircraftModel(AircraftModel model) {
        if (modelRepository.existsById(model.getModelName())) {
            throw new InvalidRequestException("Aircraft model '" + model.getModelName() + "' already exists.");
        }
        return modelRepository.save(model);
    }

    @Transactional(readOnly = true)
    public AircraftModel getAircraftModelByName(String modelName) {
        return modelRepository.findById(modelName)
                .orElseThrow(() -> new AircraftModelNotFoundException(modelName));
    }

    @Transactional(readOnly = true)
    public List<AircraftModel> getAllAircraftModels() {
        return modelRepository.findAll();
    }
}
