package com.example.AISafePSOFT_26.Aircraft.infrastructure;

import com.example.AISafePSOFT_26.Aircraft.domain.Aircraft;
import com.example.AISafePSOFT_26.Aircraft.domain.AircraftAvailability;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AircraftRepository extends JpaRepository<Aircraft, String>, JpaSpecificationExecutor<Aircraft> {

    // US103 - Consulta de aeronave por matrícula
    Optional<Aircraft> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);

    // US104 / US205 - Pesquisas por estado operacional ou modelo
    List<Aircraft> findByStatus(AircraftAvailability status);

    List<Aircraft> findByModelModelName(String modelName);

    // US104 - Pesquisa por ano de fabrico
    @Query("SELECT a FROM Aircraft a WHERE YEAR(a.manufacturingDate) = :year")
    List<Aircraft> findByManufacturingYear(@Param("year") int year);

    // US117 / Métricas - Contagem por estado operacional
    long countByStatus(AircraftAvailability status);

    // US204 - Top 5 aeronaves mais utilizadas por horas de voo
    List<Aircraft> findTop5ByOrderByTotalFlightHoursDesc();

    // US206 - Horas operacionais totais da frota
    @Query("SELECT COALESCE(SUM(a.totalFlightHours), 0.0) FROM Aircraft a")
    Double calculateTotalFleetFlightHours();
}
