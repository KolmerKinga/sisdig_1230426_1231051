package pt.isep.aisafe.maintenance.api.dto;

import pt.isep.aisafe.maintenance.domain.ChecklistItem;
import pt.isep.aisafe.maintenance.domain.MaintenanceAttribute;
import pt.isep.aisafe.maintenance.domain.MaintenanceRecord;
import pt.isep.aisafe.maintenance.domain.MaintenanceStatus;
import pt.isep.aisafe.maintenance.domain.MaintenanceType;
import pt.isep.aisafe.maintenance.domain.UsedPart;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Maintenance record representation. It is also what replicas exchange when aggregating
 * collections and reports, so every report can be computed from a list of these.
 *
 * @param aircraftSync result of updating the aircraft availability in aircraft-service after this
 *                     operation (only filled on start/complete; null otherwise)
 */
public record RecordResponse(
        UUID id,
        String aircraftRegistration,
        String aircraftModel,
        UUID templateId,
        String templateName,
        MaintenanceType operation,
        MaintenanceAttribute attribute,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Double durationHours,
        MaintenanceStatus status,
        List<ChecklistItemResponse> checklist,
        String completionNotes,
        List<UsedPartResponse> usedParts,
        Double totalCost,
        String createdBy,
        String completedBy,
        String aircraftSync) {

    public record ChecklistItemResponse(String task, boolean required, boolean done) {
        static ChecklistItemResponse from(ChecklistItem item) {
            return new ChecklistItemResponse(item.getTask(), item.isRequired(), item.isDone());
        }
    }

    public record UsedPartResponse(String partSerialNumber, Integer quantity, Double price) {
        static UsedPartResponse from(UsedPart part) {
            return new UsedPartResponse(part.getPartSerialNumber(), part.getQuantity(), part.getPrice());
        }

        public double cost() {
            return (quantity == null ? 0 : quantity) * (price == null ? 0.0 : price);
        }
    }

    public static RecordResponse from(MaintenanceRecord record) {
        return from(record, null);
    }

    public static RecordResponse from(MaintenanceRecord record, String aircraftSync) {
        return new RecordResponse(
                record.getId(),
                record.getAircraftRegistration(),
                record.getAircraftModel(),
                record.getTemplateId(),
                record.getTemplateName(),
                record.getOperation(),
                record.getAttribute(),
                record.getDescription(),
                record.getStartDate(),
                record.getEndDate(),
                record.getDurationHours(),
                record.getStatus(),
                record.getChecklist().stream().map(ChecklistItemResponse::from).toList(),
                record.getCompletionNotes(),
                record.getUsedParts().stream().map(UsedPartResponse::from).toList(),
                record.totalCost(),
                record.getCreatedBy(),
                record.getCompletedBy(),
                aircraftSync);
    }

    /** Parts of this record, never null (peers may send an empty list). */
    public List<UsedPartResponse> parts() {
        return usedParts == null ? List.of() : usedParts;
    }
}
