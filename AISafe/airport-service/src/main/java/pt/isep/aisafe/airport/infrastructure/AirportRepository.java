package pt.isep.aisafe.airport.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pt.isep.aisafe.airport.domain.Airport;

/**
 * Local store of THIS replica only. Data held by other replicas is reached through
 * peer forwarding (see PeerClient), never through this repository.
 */
public interface AirportRepository extends JpaRepository<Airport, String>, JpaSpecificationExecutor<Airport> {
}
