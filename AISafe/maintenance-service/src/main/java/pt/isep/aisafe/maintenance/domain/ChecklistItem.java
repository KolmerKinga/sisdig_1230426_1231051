package pt.isep.aisafe.maintenance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * One task of the checklist of a record, copied from the template when the record is created.
 */
@Embeddable
public class ChecklistItem {

    @Column(nullable = false)
    private String task;

    @Column(nullable = false)
    private boolean required;

    @Column(nullable = false)
    private boolean done;

    protected ChecklistItem() {
        // for JPA
    }

    public ChecklistItem(String task, boolean required) {
        if (task == null || task.isBlank()) {
            throw new InvalidMaintenanceException("Checklist task cannot be blank");
        }
        this.task = task;
        this.required = required;
        this.done = false;
    }

    /** Returns a copy with the "done" flag set (a new value, so JPA sees the change). */
    ChecklistItem withDone(boolean value) {
        ChecklistItem copy = new ChecklistItem(task, required);
        copy.done = value;
        return copy;
    }

    public String getTask() {
        return task;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isDone() {
        return done;
    }
}
