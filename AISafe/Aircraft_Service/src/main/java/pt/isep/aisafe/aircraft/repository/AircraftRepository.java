package pt.isep.aisafe.aircraft.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.isep.aisafe.aircraft.domain.Aircraft;
import pt.isep.aisafe.aircraft.domain.AircraftAvailability;

import java.util.List;
import java.util.Optional;

@Repository
public interface AircraftRepository extends JpaRepository<Aircraft, String>, JpaSpecificationExecutor<Aircraft> {

    Optional<Aircraft> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);

    List<Aircraft> findByStatus(AircraftAvailability status);

    List<Aircraft> findByModelModelName(String modelName);

    @Query("SELECT a FROM Aircraft a WHERE YEAR(a.manufacturingDate) = :year")
    List<Aircraft> findByManufacturingYear(@Param("year") int year);

    long countByStatus(AircraftAvailability status);

    List<Aircraft> findTop5ByOrderByTotalFlightHoursDesc();

    @Query("SELECT COALESCE(SUM(a.totalFlightHours), 0.0) FROM Aircraft a")
    Double calculateTotalFleetFlightHours();
}
