package pt.isep.aisafe.aircraft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.isep.aisafe.aircraft.domain.Aircraft;

@Repository
public interface AircraftRepository extends JpaRepository<Aircraft, String> {
}
