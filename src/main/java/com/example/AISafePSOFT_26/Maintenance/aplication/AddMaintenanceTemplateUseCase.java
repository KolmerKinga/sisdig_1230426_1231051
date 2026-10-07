package com.example.AISafePSOFT_26.Maintenance.aplication;

import com.example.AISafePSOFT_26.Maintenance.domain.MaintenanceAttribute;
import com.example.AISafePSOFT_26.Maintenance.domain.MaintenanceTemplate;
import com.example.AISafePSOFT_26.Maintenance.domain.MaintenanceType;
import com.example.AISafePSOFT_26.Maintenance.infrastructure.MaintenanceTemplateRepository;
import com.example.AISafePSOFT_26.UseCase;

import java.util.Map;

@UseCase
public class AddMaintenanceTemplateUseCase {
    private final MaintenanceTemplateRepository maintenanceTemplateRepository;

    public AddMaintenanceTemplateUseCase(MaintenanceTemplateRepository maintenanceTemplateRepository) {
        this.maintenanceTemplateRepository = maintenanceTemplateRepository;
    }

    public MaintenanceTemplate execute(String name, Double expectedDuration,
                                       Map<String, Boolean> templateChecklist,
                                       MaintenanceType operation,
                                       MaintenanceAttribute attribute) {
        MaintenanceTemplate template = new MaintenanceTemplate(
                name,
                expectedDuration,
                templateChecklist,
                operation,
                attribute
        );
        return maintenanceTemplateRepository.save(template);
    }

    public MaintenanceTemplate execute(MaintenanceTemplate maintenanceTemplate) {
        return maintenanceTemplateRepository.save(maintenanceTemplate);
    }
}
