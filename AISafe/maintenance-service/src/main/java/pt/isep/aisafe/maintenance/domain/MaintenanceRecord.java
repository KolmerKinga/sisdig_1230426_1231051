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
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A maintenance intervention on one aircraft (aggregate root, PSOFT US115-US119, US217-US222).
 *
 * <p>Differences from the PSOFT monolith, required by the distribution:</p>
 * <ul>
 *   <li>The aircraft is referenced by its registration number (no JPA relation to Aircraft, which
 *       belongs to aircraft-service). The model name is copied at creation for the reports.</li>
 *   <li>The template data (name, type, attribute, checklist) is copied into the record, because
 *       the template may live on another replica. Changing the attribute of a record (US217) no
 *       longer changes the shared template.</li>
 *   <li>Technicians are identified by the username in the JWT (createdBy / completedBy) instead of
 *       a relation to Collaborator, which belongs to auth-service.</li>
 *   <li>The id is a UUID so it never collides between replicas.</li>
 * </ul>
 */
@Entity
@Table(name = "maintenance_records")
public class MaintenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @Column(nullable = false)
    private String aircraftRegistration;

    private String aircraftModel;

    @Column(nullable = false)
    private UUID templateId;

    @Column(nullable = false)
    private String templateName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceType operation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceAttribute attribute;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private Double durationHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceStatus status;

    @Column(length = 2000)
    private String completionNotes;

    private String createdBy;

    private String completedBy;

    @ElementCollection
    @CollectionTable(name = "record_checklist", joinColumns = @JoinColumn(name = "record_id"))
    @OrderColumn(name = "position")
    private List<ChecklistItem> checklist = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "record_used_parts", joinColumns = @JoinColumn(name = "record_id"))
    @OrderColumn(name = "position")
    private List<UsedPart> usedParts = new ArrayList<>();

    protected MaintenanceRecord() {
        // for JPA
    }

    /**
     * US115 - plans a maintenance for an aircraft, following a template. Status INLINE.
     */
    public MaintenanceRecord(String aircraftRegistration, String aircraftModel,
                             UUID templateId, String templateName, MaintenanceType operation,
                             MaintenanceAttribute attribute, Map<String, Boolean> templateChecklist,
                             String description, LocalDate startDate, Double durationHours, String createdBy) {
        if (aircraftRegistration == null || aircraftRegistration.isBlank()) {
            throw new InvalidMaintenanceException("Aircraft registration number is required");
        }
        if (templateId == null || templateName == null || operation == null || attribute == null) {
            throw new InvalidMaintenanceException("A maintenance template is required");
        }
        if (templateChecklist == null || templateChecklist.isEmpty()) {
            throw new InvalidMaintenanceException("The template checklist cannot be empty");
        }
        if (description == null || description.isBlank()) {
            throw new InvalidMaintenanceException("Description is required");
        }
        if (startDate == null) {
            throw new InvalidMaintenanceException("Start date is required");
        }
        if (durationHours == null || durationHours <= 0) {
            throw new InvalidMaintenanceException("Duration must be positive");
        }
        this.aircraftRegistration = aircraftRegistration.trim().toUpperCase(Locale.ROOT);
        this.aircraftModel = aircraftModel;
        this.templateId = templateId;
        this.templateName = templateName;
        this.operation = operation;
        this.attribute = attribute;
        this.description = description.trim();
        this.startDate = startDate;
        this.durationHours = durationHours;
        this.createdBy = createdBy;
        this.status = MaintenanceStatus.INLINE;
        templateChecklist.forEach((task, required) ->
                this.checklist.add(new ChecklistItem(task, required == null || required)));
    }

    /** US119 - the work starts: INLINE -> ONGOING (the aircraft becomes unavailable). */
    public void start() {
        if (status != MaintenanceStatus.INLINE) {
            throw new MaintenanceRuleException("Only planned (INLINE) records can be started; current status is "
                    + status);
        }
        this.status = MaintenanceStatus.ONGOING;
    }

    /**
     * US119 - marks the record as completed with notes and the checklist result.
     * All required tasks must be done and the end date cannot be before the start date.
     */
    public void complete(String notes, Map<String, Boolean> tasksDone, LocalDate end, String user) {
        if (status == MaintenanceStatus.COMPLETED) {
            throw new MaintenanceRuleException("Maintenance record is already completed");
        }
        if (end == null) {
            throw new InvalidMaintenanceException("End date is required");
        }
        if (end.isBefore(startDate)) {
            throw new InvalidMaintenanceException("End date cannot be before start date");
        }
        Map<String, Boolean> done = tasksDone == null ? Map.of() : tasksDone;
        Set<String> known = checklist.stream().map(ChecklistItem::getTask).collect(Collectors.toSet());
        List<String> unknown = done.keySet().stream().filter(t -> !known.contains(t)).sorted().toList();
        if (!unknown.isEmpty()) {
            throw new InvalidMaintenanceException("Unknown checklist task(s): " + String.join(", ", unknown));
        }
        List<String> missing = checklist.stream()
                .filter(ChecklistItem::isRequired)
                .filter(item -> !Boolean.TRUE.equals(done.get(item.getTask())))
                .map(ChecklistItem::getTask)
                .toList();
        if (!missing.isEmpty()) {
            throw new MaintenanceRuleException("Required checklist task(s) not done: " + String.join(", ", missing));
        }
        checklist.replaceAll(item -> item.withDone(Boolean.TRUE.equals(done.get(item.getTask()))));
        this.completionNotes = notes;
        this.endDate = end;
        this.completedBy = user;
        this.status = MaintenanceStatus.COMPLETED;
    }

    /** Registers a part used in this maintenance (not allowed after completion). */
    public void addUsedPart(UsedPart part) {
        if (part == null) {
            throw new InvalidMaintenanceException("Used part is required");
        }
        if (status == MaintenanceStatus.COMPLETED) {
            throw new MaintenanceRuleException("Cannot add parts to a completed maintenance record");
        }
        usedParts.add(part);
    }

    /** US217 - categorize the record by maintenance component. */
    public void changeAttribute(MaintenanceAttribute newAttribute) {
        if (newAttribute == null) {
            throw new InvalidMaintenanceException("Attribute is required");
        }
        this.attribute = newAttribute;
    }

    public double totalCost() {
        return usedParts.stream().mapToDouble(UsedPart::cost).sum();
    }

    public boolean isOngoing() {
        return status == MaintenanceStatus.ONGOING;
    }

    public UUID getId() {
        return id;
    }

    public String getAircraftRegistration() {
        return aircraftRegistration;
    }

    public String getAircraftModel() {
        return aircraftModel;
    }

    public UUID getTemplateId() {
        return templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public MaintenanceType getOperation() {
        return operation;
    }

    public MaintenanceAttribute getAttribute() {
        return attribute;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Double getDurationHours() {
        return durationHours;
    }

    public MaintenanceStatus getStatus() {
        return status;
    }

    public String getCompletionNotes() {
        return completionNotes;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getCompletedBy() {
        return completedBy;
    }

    public List<ChecklistItem> getChecklist() {
        return Collections.unmodifiableList(checklist);
    }

    public List<UsedPart> getUsedParts() {
        return Collections.unmodifiableList(usedParts);
    }
}
