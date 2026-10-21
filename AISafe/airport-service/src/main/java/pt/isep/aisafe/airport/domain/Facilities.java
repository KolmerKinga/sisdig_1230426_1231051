package pt.isep.aisafe.airport.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Value object describing terminals, gates and services of an airport (PSOFT US207).
 */
@Embeddable
public class Facilities {

    @Column(nullable = false)
    private Integer terminalCount;

    @Column(nullable = false)
    private Integer gateCount;

    @ElementCollection
    @CollectionTable(name = "airport_services", joinColumns = @JoinColumn(name = "airport_iata_code"))
    @Column(name = "service")
    private List<String> services = new ArrayList<>();

    protected Facilities() {
        // for JPA
    }

    public Facilities(Integer terminalCount, Integer gateCount, List<String> services) {
        if (terminalCount == null || terminalCount < 0) {
            throw new InvalidAirportException("Terminal count must be zero or positive");
        }
        if (gateCount == null || gateCount < 0) {
            throw new InvalidAirportException("Gate count must be zero or positive");
        }
        this.terminalCount = terminalCount;
        this.gateCount = gateCount;
        this.services = services == null ? new ArrayList<>() : new ArrayList<>(services);
    }

    public Integer getTerminalCount() {
        return terminalCount;
    }

    public Integer getGateCount() {
        return gateCount;
    }

    public List<String> getServices() {
        return services == null ? List.of() : Collections.unmodifiableList(services);
    }
}
