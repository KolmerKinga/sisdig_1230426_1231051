package com.example.AISafePSOFT_26.Maintenance.infrastructure;

import com.example.AISafePSOFT_26.Maintenance.domain.MaintenanceTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MaintenanceTemplateRepository extends JpaRepository<MaintenanceTemplate, Long> {
    Optional<MaintenanceTemplate> findByTemplateId(Long recordId);
}
