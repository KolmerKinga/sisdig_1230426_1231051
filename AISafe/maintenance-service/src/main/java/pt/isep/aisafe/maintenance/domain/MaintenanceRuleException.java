package pt.isep.aisafe.maintenance.domain;

/**
 * The operation is not allowed in the current state of the record, e.g. completing a record that
 * is already completed or with required checklist tasks not done (HTTP 409).
 */
public class MaintenanceRuleException extends RuntimeException {
    public MaintenanceRuleException(String message) {
        super(message);
    }
}
