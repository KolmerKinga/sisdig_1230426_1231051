package pt.isep.aisafe.maintenance.api.dto;

import pt.isep.aisafe.maintenance.domain.MaintenanceAttribute;
import pt.isep.aisafe.maintenance.domain.MaintenanceTemplate;
import pt.isep.aisafe.maintenance.domain.MaintenanceType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Template representation (also exchanged between replicas).
 */
public record TemplateResponse(UUID id, String name, Double expectedDuration,
                               Map<String, Boolean> templateChecklist,
                               MaintenanceType operation, MaintenanceAttribute attribute) {

    public static TemplateResponse from(MaintenanceTemplate template) {
        return new TemplateResponse(template.getId(), template.getName(), template.getExpectedDuration(),
                new LinkedHashMap<>(template.getChecklist()), template.getOperation(), template.getAttribute());
    }
}
