package com.example.AISafePSOFT_26.Maintenance.aplication;

import com.example.AISafePSOFT_26.Aircraft.domain.Aircraft;
import com.example.AISafePSOFT_26.Maintenance.domain.MaintenanceRecord;
import com.example.AISafePSOFT_26.Maintenance.domain.MaintenanceTemplate;
import com.example.AISafePSOFT_26.Maintenance.infrastructure.MaintenanceRecordRepository;
import com.example.AISafePSOFT_26.UseCase;

import java.time.LocalDate;


/**
 * Use case: a Maintenance Technician adds a new maintenance record.
 *
 * <p>aircraft
 * registration, maintenance type (according to a maintenance template), description, start date, expected duration and
 * its checklist (defined by the maintenance template)
 *
 */

@UseCase
public class AddMaintenanceRecordUseCase {
    private final MaintenanceRecordRepository maintenanceRecordRepository;

    public AddMaintenanceRecordUseCase(MaintenanceRecordRepository maintenanceRecordRepository) {
        this.maintenanceRecordRepository = maintenanceRecordRepository;
    }

    /**
     * Looks up the model for the given modelName, creates a {@link com.example.AISafePSOFT_26.Maintenance.domain.MaintenanceRecord}
     *with the given {@link com.example.AISafePSOFT_26.Aircraft.domain.Aircraft},manufacturer,name and persists it.
     * @param aircraft, the aircraft to do the maintenance
     * @param maintenanceTemplate, the Template of maintenance;
     * @param description the description of the maintenance;
     * @param startDate, the day of maintenance starts;
     * @param durationHours the expected duration of the maintenance;
     */
    public void executeShort(Aircraft aircraft,MaintenanceTemplate maintenanceTemplate,Double durationHours, String description, LocalDate startDate) {
        maintenanceRecordRepository.save(new MaintenanceRecord(aircraft,maintenanceTemplate,durationHours,description,startDate));
    }

    /**
     * Adds a record to persist
     * @param maintenanceRecord the model object to be persisted
     */
    public void execute(MaintenanceRecord maintenanceRecord) {
        maintenanceRecordRepository.save(maintenanceRecord);
    }
}


