package pt.isep.aisafe.maintenance.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Maintenance procedure (PSOFT US115): type, component, expected duration and checklist.
 * The checklist maps each task to "required" (true) or "optional" (false).
 *
 * <p>The id is a UUID so it never collides between replicas.</p>
 */
@Entity
@Table(name = "maintenance_templates")
public class MaintenanceTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceType operation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceAttribute attribute;

    @Column(nullable = false)
    private Double expectedDuration;

    @ElementCollection
    @CollectionTable(name = "template_checklist", joinColumns = @JoinColumn(name = "template_id"))
    @MapKeyColumn(name = "task")
    @Column(name = "required", nullable = false)
    private Map<String, Boolean> checklist = new LinkedHashMap<>();

    protected MaintenanceTemplate() {
        // for JPA
    }

    public MaintenanceTemplate(String name, MaintenanceType operation, MaintenanceAttribute attribute,
                               Double expectedDuration, Map<String, Boolean> checklist) {
        if (name == null || name.isBlank()) {
            throw new InvalidMaintenanceException("Template name is required");
        }
        if (operation == null || attribute == null) {
            throw new InvalidMaintenanceException("Template operation and attribute are required");
        }
        if (expectedDuration == null || expectedDuration <= 0) {
            throw new InvalidMaintenanceException("Expected duration must be positive");
        }
        if (checklist == null || checklist.isEmpty()) {
            throw new InvalidMaintenanceException("The checklist must have at least one task");
        }
        Map<String, Boolean> items = new LinkedHashMap<>();
        checklist.forEach((task, required) -> {
            if (task == null || task.isBlank()) {
                throw new InvalidMaintenanceException("Checklist tasks cannot be blank");
            }
            items.put(task.trim(), required == null || required);
        });
        this.name = name.trim();
        this.operation = operation;
        this.attribute = attribute;
        this.expectedDuration = expectedDuration;
        this.checklist = items;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public MaintenanceType getOperation() {
        return operation;
    }

    public MaintenanceAttribute getAttribute() {
        return attribute;
    }

    public Double getExpectedDuration() {
        return expectedDuration;
    }

    public Map<String, Boolean> getChecklist() {
        return Collections.unmodifiableMap(checklist);
    }
}
