package com.example.AISafePSOFT_26.Aircraft.infrastructure;

import com.example.AISafePSOFT_26.Aircraft.domain.Aircraft;
import com.example.AISafePSOFT_26.Aircraft.domain.AircraftAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AircraftRepository extends JpaRepository<Aircraft, String>,JpaSpecificationExecutor<Aircraft> {
    Optional<Aircraft> findByRegistrationNumber(String registrationNumber);
    long countByStatus(AircraftAvailability availability);
}