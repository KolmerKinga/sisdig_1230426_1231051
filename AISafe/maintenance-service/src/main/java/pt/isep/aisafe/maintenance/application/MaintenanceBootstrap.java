package pt.isep.aisafe.maintenance.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.aisafe.maintenance.domain.MaintenanceAttribute;
import pt.isep.aisafe.maintenance.domain.MaintenanceRecord;
import pt.isep.aisafe.maintenance.domain.MaintenanceStatus;
import pt.isep.aisafe.maintenance.domain.MaintenanceTemplate;
import pt.isep.aisafe.maintenance.domain.MaintenanceType;
import pt.isep.aisafe.maintenance.domain.UsedPart;
import pt.isep.aisafe.maintenance.infrastructure.MaintenanceRecordRepository;
import pt.isep.aisafe.maintenance.infrastructure.MaintenanceTemplateRepository;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads the seed templates and records of THIS replica (aisafe.bootstrap.file).
 * Each replica gets different data, so the demo shows partitioning, forwarding and aggregation.
 *
 * <p>The seed records already contain the aircraft model (no call to aircraft-service at start-up,
 * which may not be running yet).</p>
 */
@Component
public class MaintenanceBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceBootstrap.class);

    public record SeedFile(List<SeedTemplate> templates, List<SeedRecord> records) {
    }

    public record SeedTemplate(String name, MaintenanceType operation, MaintenanceAttribute attribute,
                               Double expectedDuration, Map<String, Boolean> templateChecklist) {
    }

    public record SeedPart(String partSerialNumber, Integer quantity, Double price) {
    }

    public record SeedRecord(String templateName, String aircraftRegistration, String aircraftModel,
                             String description, LocalDate startDate, Double durationHours,
                             MaintenanceStatus status, LocalDate endDate, String completionNotes,
                             List<SeedPart> usedParts, String createdBy, String completedBy) {
    }

    private final MaintenanceTemplateRepository templates;
    private final MaintenanceRecordRepository records;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final String seedFile;

    public MaintenanceBootstrap(MaintenanceTemplateRepository templates, MaintenanceRecordRepository records,
                                ObjectMapper objectMapper, ResourceLoader resourceLoader,
                                @Value("${aisafe.bootstrap.file:}") String seedFile) {
        this.templates = templates;
        this.records = records;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.seedFile = seedFile;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (seedFile == null || seedFile.isBlank() || templates.count() > 0 || records.count() > 0) {
            return;
        }
        Resource resource = resourceLoader.getResource(seedFile);
        if (!resource.exists()) {
            log.warn("Seed file {} not found; starting with an empty store", seedFile);
            return;
        }
        SeedFile seed;
        try (InputStream input = resource.getInputStream()) {
            seed = objectMapper.readValue(input, SeedFile.class);
        }

        Map<String, MaintenanceTemplate> byName = new HashMap<>();
        for (SeedTemplate t : seed.templates()) {
            MaintenanceTemplate template = templates.save(new MaintenanceTemplate(
                    t.name(), t.operation(), t.attribute(), t.expectedDuration(), t.templateChecklist()));
            byName.put(template.getName(), template);
        }

        for (SeedRecord r : seed.records()) {
            MaintenanceTemplate template = byName.get(r.templateName());
            if (template == null) {
                log.warn("Seed record for {} skipped: template '{}' not in this seed file",
                        r.aircraftRegistration(), r.templateName());
                continue;
            }
            MaintenanceRecord record = new MaintenanceRecord(r.aircraftRegistration(), r.aircraftModel(),
                    template.getId(), template.getName(), template.getOperation(), template.getAttribute(),
                    template.getChecklist(), r.description(), r.startDate(),
                    r.durationHours() != null ? r.durationHours() : template.getExpectedDuration(), r.createdBy());
            if (r.usedParts() != null) {
                r.usedParts().forEach(p -> record.addUsedPart(new UsedPart(p.partSerialNumber(), p.quantity(), p.price())));
            }
            if (r.status() == MaintenanceStatus.ONGOING || r.status() == MaintenanceStatus.COMPLETED) {
                record.start();
            }
            if (r.status() == MaintenanceStatus.COMPLETED) {
                Map<String, Boolean> allDone = new HashMap<>();
                template.getChecklist().keySet().forEach(task -> allDone.put(task, true));
                record.complete(r.completionNotes(), allDone, r.endDate(), r.completedBy());
            }
            records.save(record);
        }
        log.info("Bootstrap: {} template(s) and {} record(s) loaded from {}",
                seed.templates().size(), seed.records().size(), seedFile);
    }
}
