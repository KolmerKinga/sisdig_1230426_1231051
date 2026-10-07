package com.example.AISafePSOFT_26.Maintenance.infrastructure;

import com.example.AISafePSOFT_26.Maintenance.domain.MaintenanceRecord;
import com.example.AISafePSOFT_26.Maintenance.domain.MaintenanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {
    Optional<MaintenanceRecord> findByRecordId(Long recordId);
    List<MaintenanceRecord> findByAircraft_RegistrationNumber(String registrationNumber);
    List<MaintenanceRecord> findByStatus(MaintenanceStatus status);

    Page<MaintenanceRecord> findAll(Specification<MaintenanceRecord> spec, Pageable pageable);
}
