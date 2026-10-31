package pt.isep.aisafe.aircraft.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pt.isep.aisafe.aircraft.domain.AircraftModel;

import java.util.Optional;

@Repository
public interface AircraftModelRepository extends JpaRepository<AircraftModel, String>, JpaSpecificationExecutor<AircraftModel> {

    Optional<AircraftModel> findByModelName(String modelName);

    boolean existsByModelName(String modelName);
}
