package com.example.AISafePSOFT_26.Airport.infrastructure;

import com.example.AISafePSOFT_26.Airport.domain.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AirportRepository extends JpaRepository<Airport, String>, JpaSpecificationExecutor<Airport> {
    Optional<Airport> findByIataCode(String iataCode);
    Optional<Airport> findByName(String airportName);
}
