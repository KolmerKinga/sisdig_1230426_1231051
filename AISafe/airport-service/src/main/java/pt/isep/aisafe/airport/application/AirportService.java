package pt.isep.aisafe.airport.application;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.aisafe.airport.domain.Airport;
import pt.isep.aisafe.airport.domain.AirportStatus;
import pt.isep.aisafe.airport.domain.Certification;
import pt.isep.aisafe.airport.domain.Contact;
import pt.isep.aisafe.airport.infrastructure.AirportRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Use cases of the Airport Management context, applied to the LOCAL store of this replica.
 * Distribution concerns (forwarding to peers, aggregation) live in the API layer.
 */
@Service
@Transactional
public class AirportService {

    private final AirportRepository repository;

    public AirportService(AirportRepository repository) {
        this.repository = repository;
    }

    /** US106 / US207 - register an airport on this replica. */
    public Airport register(Airport airport) {
        if (repository.existsById(airport.getIataCode())) {
            throw new AirportAlreadyExistsException(airport.getIataCode(), "this replica");
        }
        return repository.save(airport);
    }

    /** US107 - airport details by IATA code (local store only). */
    @Transactional(readOnly = true)
    public Optional<Airport> findLocal(String iataCode) {
        return repository.findById(normalize(iataCode));
    }

    @Transactional(readOnly = true)
    public boolean existsLocally(String iataCode) {
        return repository.existsById(normalize(iataCode));
    }

    /** US108 - search by city, country and/or name (case-insensitive, local store only). */
    @Transactional(readOnly = true)
    public List<Airport> searchLocal(String city, String country, String name) {
        Specification<Airport> spec = (root, query, cb) -> cb.conjunction();
        if (hasText(city)) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("location").<String>get("city")), city.toLowerCase(Locale.ROOT)));
        }
        if (hasText(country)) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("location").<String>get("country")), country.toLowerCase(Locale.ROOT)));
        }
        if (hasText(name)) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.<String>get("name")), "%" + name.toLowerCase(Locale.ROOT) + "%"));
        }
        return repository.findAll(spec, Sort.by("iataCode"));
    }

    /** US106a - add a certification to an airport held by this replica. */
    public Certification addCertification(String iataCode, Certification certification) {
        Airport airport = getLocalOrThrow(iataCode);
        airport.addCertification(certification);
        // the aggregate is managed: flushing cascades the insert and assigns the UUID
        repository.flush();
        return certification;
    }

    /** US109 - change the operational status. */
    public Airport updateStatus(String iataCode, AirportStatus status) {
        Airport airport = getLocalOrThrow(iataCode);
        airport.changeStatus(status);
        repository.flush(); // optimistic locking (@Version) is checked here
        return airport;
    }

    /** US208 - update name, operational hours and contacts. */
    public Airport updateDetails(String iataCode, String name, String operationalHours, List<Contact> contacts) {
        Airport airport = getLocalOrThrow(iataCode);
        airport.updateDetails(name, operationalHours, contacts);
        repository.flush();
        return airport;
    }

    @Transactional(readOnly = true)
    public long countLocal() {
        return repository.count();
    }

    private Airport getLocalOrThrow(String iataCode) {
        return repository.findById(normalize(iataCode))
                .orElseThrow(() -> new AirportNotFoundException(normalize(iataCode), false));
    }

    public static String normalize(String iataCode) {
        return iataCode == null ? null : iataCode.trim().toUpperCase(Locale.ROOT);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
