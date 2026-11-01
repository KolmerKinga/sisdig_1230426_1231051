package pt.isep.aisafe.maintenance.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pt.isep.aisafe.maintenance.domain.MaintenanceRecord;

import java.util.UUID;

/**
 * Records stored on THIS replica only. Records of other replicas are reached through
 * peer forwarding / aggregation, never through this repository.
 */
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, UUID>,
        JpaSpecificationExecutor<MaintenanceRecord> {
}
