package pt.isep.aisafe.maintenance.application;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.aisafe.maintenance.domain.MaintenanceAttribute;
import pt.isep.aisafe.maintenance.domain.MaintenanceRecord;
import pt.isep.aisafe.maintenance.domain.MaintenanceTemplate;
import pt.isep.aisafe.maintenance.domain.UsedPart;
import pt.isep.aisafe.maintenance.infrastructure.MaintenanceRecordRepository;
import pt.isep.aisafe.maintenance.infrastructure.MaintenanceTemplateRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Use cases of the Maintenance Management context, applied to the LOCAL store of this replica.
 * Distribution concerns (forwarding to peers, aggregation, calls to aircraft-service) live in the
 * API layer.
 */
@Service
@Transactional
public class MaintenanceService {

    private final MaintenanceTemplateRepository templates;
    private final MaintenanceRecordRepository records;

    public MaintenanceService(MaintenanceTemplateRepository templates, MaintenanceRecordRepository records) {
        this.templates = templates;
        this.records = records;
    }

    // ------------------------------------------------------------------ templates

    /** US115 (templates) - create a maintenance template on this replica. */
    public MaintenanceTemplate createTemplate(MaintenanceTemplate template) {
        return templates.save(template);
    }

    @Transactional(readOnly = true)
    public Optional<MaintenanceTemplate> findTemplate(UUID id) {
        return templates.findById(id);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceTemplate> allTemplates() {
        return templates.findAll(Sort.by("name"));
    }

    // ------------------------------------------------------------------ records

    /** US115 - store a new maintenance record on this replica. */
    public MaintenanceRecord createRecord(MaintenanceRecord record) {
        return records.save(record);
    }

    @Transactional(readOnly = true)
    public Optional<MaintenanceRecord> findRecord(UUID id) {
        return records.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean recordExists(UUID id) {
        return records.existsById(id);
    }

    /** US116 / US218 - records of this replica matching the filter, most recent first. */
    @Transactional(readOnly = true)
    public List<MaintenanceRecord> searchRecords(RecordFilter filter) {
        Specification<MaintenanceRecord> spec = (root, query, cb) -> cb.conjunction();
        if (filter.aircraft() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.upper(root.<String>get("aircraftRegistration")), filter.aircraft()));
        }
        if (filter.attribute() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("attribute"), filter.attribute()));
        }
        if (filter.status() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), filter.status()));
        }
        if (filter.initialDate() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.<LocalDate>get("startDate"), filter.initialDate()));
        }
        if (filter.finalDate() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.<LocalDate>get("startDate"), filter.finalDate()));
        }
        return records.findAll(spec, Sort.by(Sort.Direction.DESC, "startDate"));
    }

    /** US119 - work starts (INLINE -> ONGOING). */
    public MaintenanceRecord startRecord(UUID id) {
        MaintenanceRecord record = getRecord(id);
        record.start();
        records.flush(); // optimistic locking (@Version) is checked here
        return record;
    }

    /** US119 - mark as completed with notes and checklist result. */
    public MaintenanceRecord completeRecord(UUID id, String notes, Map<String, Boolean> tasksDone,
                                            LocalDate endDate, String user) {
        MaintenanceRecord record = getRecord(id);
        record.complete(notes, tasksDone, endDate, user);
        records.flush();
        return record;
    }

    /** US220 / US226 - register a used part. */
    public MaintenanceRecord addUsedPart(UUID id, UsedPart part) {
        MaintenanceRecord record = getRecord(id);
        record.addUsedPart(part);
        records.flush();
        return record;
    }

    /** US217 - categorize by maintenance component. */
    public MaintenanceRecord changeAttribute(UUID id, MaintenanceAttribute attribute) {
        MaintenanceRecord record = getRecord(id);
        record.changeAttribute(attribute);
        records.flush();
        return record;
    }

    @Transactional(readOnly = true)
    public long countRecords() {
        return records.count();
    }

    private MaintenanceRecord getRecord(UUID id) {
        return records.findById(id).orElseThrow(() -> new RecordNotFoundException(id, false));
    }
}
