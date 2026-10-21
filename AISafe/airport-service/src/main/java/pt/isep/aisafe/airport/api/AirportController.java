package pt.isep.aisafe.airport.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.isep.aisafe.airport.api.dto.AirportResponse;
import pt.isep.aisafe.airport.api.dto.AirportResponse.CertificationResponse;
import pt.isep.aisafe.airport.api.dto.CreateAirportRequest;
import pt.isep.aisafe.airport.api.dto.CreateAirportRequest.CertificationRequest;
import pt.isep.aisafe.airport.api.dto.ImportReport;
import pt.isep.aisafe.airport.api.dto.UpdateDetailsRequest;
import pt.isep.aisafe.airport.api.dto.UpdateStatusRequest;
import pt.isep.aisafe.airport.application.AirportAlreadyExistsException;
import pt.isep.aisafe.airport.application.AirportNotFoundException;
import pt.isep.aisafe.airport.application.AirportService;
import pt.isep.aisafe.airport.application.InvalidRequestException;
import pt.isep.aisafe.airport.domain.Airport;
import pt.isep.aisafe.airport.domain.Certification;
import pt.isep.aisafe.airport.domain.InvalidAirportException;
import pt.isep.aisafe.airport.shared.forwarding.ForwardingHeaders;
import pt.isep.aisafe.airport.shared.forwarding.PeerClient;
import pt.isep.aisafe.airport.shared.forwarding.PeerCollection;
import pt.isep.aisafe.airport.shared.forwarding.PeerLookup;
import pt.isep.aisafe.airport.shared.forwarding.PeerResponse;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * REST API of the Airport Management context.
 *
 * <p>Distribution rules (same on every replica):</p>
 * <ul>
 *   <li>Single resource (GET, PATCH, POST certification): use the local store; if the airport is
 *       not here, send the same request to the peers and return the answer of the peer that has it.</li>
 *   <li>Collections (search, grouped): local results + results of every peer, merged without
 *       duplicates (by IATA code).</li>
 *   <li>A request that already carries X-Forwarded-By is answered locally only (max 1 hop).</li>
 *   <li>Writes stay on the replica that received them (data partitioning); duplicates of the
 *       natural key (IATA code) are checked on the peers before creating.</li>
 * </ul>
 */
@RestController
@RequestMapping(AirportController.BASE)
public class AirportController {

    static final String BASE = "/api/airports";
    private static final String LOCAL = "local";
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> GROUPING_CRITERIA = Set.of("country", "city", "region");

    private final AirportService airportService;
    private final PeerClient peerClient;
    private final AirportCsvParser csvParser;
    private final Validator validator;

    public AirportController(AirportService airportService, PeerClient peerClient,
                             AirportCsvParser csvParser, Validator validator) {
        this.airportService = airportService;
        this.peerClient = peerClient;
        this.csvParser = csvParser;
        this.validator = validator;
    }

    // ------------------------------------------------------------------ commands

    /** US106 / US207 - register an airport (stored on the replica that receives the request). */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AirportResponse> register(@Valid @RequestBody CreateAirportRequest request,
                                                    HttpServletRequest http) {
        Registration registration = registerOnThisReplica(request, ForwardingHeaders.isForwarded(http));
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{iataCode}")
                .buildAndExpand(registration.airport().getIataCode())
                .toUri();
        return ResponseEntity.created(location)
                .header(ForwardingHeaders.DATA_SOURCE, LOCAL)
                .header(ForwardingHeaders.PARTIAL_RESULT, String.valueOf(registration.partial()))
                .body(AirportResponse.from(registration.airport()));
    }

    /** US106a - add a certification. Forwarded to the replica that holds the airport. */
    @PostMapping(value = "/{iataCode}/certifications", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addCertification(@PathVariable String iataCode,
                                              @Valid @RequestBody CertificationRequest request,
                                              HttpServletRequest http) {
        if (airportService.existsLocally(iataCode)) {
            Certification certification = airportService.addCertification(iataCode, request.toDomain());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header(ForwardingHeaders.DATA_SOURCE, LOCAL)
                    .body(CertificationResponse.from(certification));
        }
        return answerFromPeers(HttpMethod.POST, http, request, iataCode);
    }

    /** US109 - change the operational status. Forwarded to the replica that holds the airport. */
    @PatchMapping(value = "/{iataCode}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateStatus(@PathVariable String iataCode,
                                          @Valid @RequestBody UpdateStatusRequest request,
                                          HttpServletRequest http) {
        if (airportService.existsLocally(iataCode)) {
            Airport airport = airportService.updateStatus(iataCode, request.status());
            return local(AirportResponse.from(airport));
        }
        return answerFromPeers(HttpMethod.PATCH, http, request, iataCode);
    }

    /** US208 - update name, operational hours and contacts. Forwarded to the replica that holds the airport. */
    @PatchMapping(value = "/{iataCode}/details", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateDetails(@PathVariable String iataCode,
                                           @Valid @RequestBody UpdateDetailsRequest request,
                                           HttpServletRequest http) {
        if (airportService.existsLocally(iataCode)) {
            Airport airport = airportService.updateDetails(iataCode, request.name(),
                    request.operationalHours(), request.contactsAsDomain());
            return local(AirportResponse.from(airport));
        }
        return answerFromPeers(HttpMethod.PATCH, http, request, iataCode);
    }

    /** US225 - bulk import from CSV. Each new airport is stored on this replica. */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportReport> importCsv(@RequestParam("file") MultipartFile file,
                                                  HttpServletRequest http) throws IOException {
        if (file.isEmpty()) {
            throw new InvalidRequestException("The CSV file is empty");
        }
        boolean forwarded = ForwardingHeaders.isForwarded(http);
        int imported = 0;
        boolean partial = false;
        List<String> skipped = new ArrayList<>();

        for (AirportCsvParser.ParsedRow row : csvParser.parse(file.getInputStream())) {
            if (row.error() != null) {
                skipped.add("line " + row.line() + ": " + row.error());
                continue;
            }
            Set<ConstraintViolation<CreateAirportRequest>> violations = validator.validate(row.request());
            if (!violations.isEmpty()) {
                skipped.add("line " + row.line() + ": " + violations.stream()
                        .map(v -> v.getPropertyPath() + " " + v.getMessage())
                        .collect(Collectors.joining(", ")));
                continue;
            }
            try {
                Registration registration = registerOnThisReplica(row.request(), forwarded);
                partial = partial || registration.partial();
                imported++;
            } catch (AirportAlreadyExistsException | InvalidAirportException e) {
                skipped.add("line " + row.line() + ": " + e.getMessage());
            }
        }
        return ResponseEntity.ok()
                .header(ForwardingHeaders.PARTIAL_RESULT, String.valueOf(partial))
                .body(new ImportReport(imported, skipped, partial));
    }

    // ------------------------------------------------------------------ queries

    /** US107 - airport details by IATA code: local store first, then peers. */
    @GetMapping("/{iataCode}")
    public ResponseEntity<?> getAirport(@PathVariable String iataCode, HttpServletRequest http) {
        return airportService.findLocal(iataCode)
                .<ResponseEntity<?>>map(airport -> local(AirportResponse.from(airport)))
                .orElseGet(() -> answerFromPeers(HttpMethod.GET, http, null, iataCode));
    }

    /** US108 - search by city, country and/or name, aggregated from all replicas and paginated. */
    @GetMapping
    public ResponseEntity<List<AirportResponse>> search(@RequestParam(required = false) String city,
                                                        @RequestParam(required = false) String country,
                                                        @RequestParam(required = false) String name,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size,
                                                        HttpServletRequest http) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidRequestException("page must be >= 0 and size between 1 and " + MAX_PAGE_SIZE);
        }
        List<AirportResponse> local = airportService.searchLocal(city, country, name).stream()
                .map(AirportResponse::from)
                .toList();

        // Internal call from a peer: return the full local result, the caller merges and paginates.
        if (ForwardingHeaders.isForwarded(http)) {
            return ResponseEntity.ok().header(ForwardingHeaders.DATA_SOURCE, LOCAL).body(local);
        }

        PeerCollection<AirportResponse> remote =
                peerClient.collectFromPeers(ForwardingHeaders.pathAndQuery(http), AirportResponse.class);
        List<AirportResponse> merged = mergeByIata(local, remote.items());

        int from = Math.min(page * size, merged.size());
        int to = Math.min(from + size, merged.size());
        return ResponseEntity.ok()
                .header(ForwardingHeaders.TOTAL_COUNT, String.valueOf(merged.size()))
                .header(ForwardingHeaders.PARTIAL_RESULT, String.valueOf(remote.partial()))
                .body(merged.subList(from, to));
    }

    /** US211 - airports grouped by country, city or region, aggregated from all replicas. */
    @GetMapping("/grouped")
    public ResponseEntity<Map<String, List<AirportResponse>>> grouped(
            @RequestParam(defaultValue = "country") String by, HttpServletRequest http) {
        String criteria = by.toLowerCase(java.util.Locale.ROOT);
        if (!GROUPING_CRITERIA.contains(criteria)) {
            throw new InvalidRequestException("Unknown grouping criteria '" + by + "'; use one of " + GROUPING_CRITERIA);
        }
        List<AirportResponse> local = airportService.searchLocal(null, null, null).stream()
                .map(AirportResponse::from)
                .toList();

        boolean partial = false;
        List<AirportResponse> all = local;
        if (!ForwardingHeaders.isForwarded(http)) {
            // ask each peer for its full local list (search without filters)
            PeerCollection<AirportResponse> remote = peerClient.collectFromPeers(BASE, AirportResponse.class);
            all = mergeByIata(local, remote.items());
            partial = remote.partial();
        }

        Function<AirportResponse, String> key = switch (criteria) {
            case "city" -> a -> a.location().city();
            case "region" -> a -> a.location().region();
            default -> a -> a.location().country();
        };
        Map<String, List<AirportResponse>> groups = all.stream()
                .collect(Collectors.groupingBy(key, TreeMap::new, Collectors.toList()));
        return ResponseEntity.ok()
                .header(ForwardingHeaders.PARTIAL_RESULT, String.valueOf(partial))
                .body(groups);
    }

    // ------------------------------------------------------------------ helpers

    private record Registration(Airport airport, boolean partial) {
    }

    /**
     * Stores the airport on this replica after checking that no peer already has the IATA code.
     * If a peer cannot be checked the airport is still created (availability first, AP) and the
     * answer is flagged as partial.
     */
    private Registration registerOnThisReplica(CreateAirportRequest request, boolean forwarded) {
        String iataCode = request.iataCode();
        boolean partial = false;
        if (!forwarded) {
            if (airportService.existsLocally(iataCode)) {
                throw new AirportAlreadyExistsException(iataCode, "this replica");
            }
            PeerLookup lookup = peerClient.forwardToPeers(HttpMethod.GET, BASE + "/" + iataCode, null);
            if (lookup.found()) {
                throw new AirportAlreadyExistsException(iataCode, "stored on " + lookup.response().get().peer());
            }
            partial = lookup.partial() || lookup.response().isPresent();
        }
        return new Registration(airportService.register(request.toDomain()), partial);
    }

    /**
     * The airport is not on this replica: ask the peers and pass through the answer of the
     * replica that holds it. A forwarded request is never forwarded again.
     */
    private ResponseEntity<?> answerFromPeers(HttpMethod method, HttpServletRequest http, Object body,
                                              String iataCode) {
        String normalized = AirportService.normalize(iataCode);
        if (ForwardingHeaders.isForwarded(http)) {
            throw new AirportNotFoundException(normalized, false);
        }
        PeerLookup lookup = peerClient.forwardToPeers(method, ForwardingHeaders.pathAndQuery(http), body);
        if (lookup.response().isEmpty()) {
            throw new AirportNotFoundException(normalized, lookup.partial());
        }
        PeerResponse answer = lookup.response().get();
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(answer.status())
                .header(ForwardingHeaders.DATA_SOURCE, answer.peer())
                .header(ForwardingHeaders.PARTIAL_RESULT, String.valueOf(lookup.partial()));
        if (answer.contentType() != null) {
            builder.contentType(answer.contentType());
        }
        return builder.body(answer.body());
    }

    private static ResponseEntity<AirportResponse> local(AirportResponse body) {
        return ResponseEntity.ok().header(ForwardingHeaders.DATA_SOURCE, LOCAL).body(body);
    }

    /** Local items win over peer copies with the same IATA code; result sorted by IATA code. */
    private static List<AirportResponse> mergeByIata(List<AirportResponse> local, List<AirportResponse> remote) {
        Map<String, AirportResponse> byIata = new LinkedHashMap<>();
        local.forEach(a -> byIata.putIfAbsent(a.iataCode(), a));
        remote.forEach(a -> byIata.putIfAbsent(a.iataCode(), a));
        return byIata.values().stream()
                .sorted(Comparator.comparing(AirportResponse::iataCode))
                .toList();
    }
}
