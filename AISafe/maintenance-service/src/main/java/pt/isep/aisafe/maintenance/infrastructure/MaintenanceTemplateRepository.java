package pt.isep.aisafe.maintenance.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.isep.aisafe.maintenance.domain.MaintenanceTemplate;

import java.util.UUID;

/** Templates stored on THIS replica only. */
public interface MaintenanceTemplateRepository extends JpaRepository<MaintenanceTemplate, UUID> {
}
