package com.example.AISafePSOFT_26.WP4A.US116;

import com.example.AISafePSOFT_26.Aircraft.domain.Aircraft;
import com.example.AISafePSOFT_26.Maintenance.aplication.AddMaintenanceRecordUseCase;
import com.example.AISafePSOFT_26.Maintenance.domain.MaintenanceRecord;
import com.example.AISafePSOFT_26.Maintenance.domain.MaintenanceStatus;
import com.example.AISafePSOFT_26.Maintenance.domain.MaintenanceTemplate;
import com.example.AISafePSOFT_26.Maintenance.infrastructure.MaintenanceRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogMaintenanceActivityUseCaseToRepoTest {
    private MaintenanceRecordRepository maintenanceRecordRepository;
    private AddMaintenanceRecordUseCase useCase;

    @BeforeEach
    void setUp() {
        maintenanceRecordRepository = mock(MaintenanceRecordRepository.class);
        useCase = new AddMaintenanceRecordUseCase(maintenanceRecordRepository);
    }

    @Test
    void shouldSaveMaintenanceRecordWhenExecuteShortWithFields() {
        Aircraft aircraft = mock(Aircraft.class);
        MaintenanceTemplate template = mock(MaintenanceTemplate.class);
        LocalDate startDate = LocalDate.of(2026, 5, 21);

        useCase.executeShort(
                aircraft,
                template,
                6.5,
                "Scheduled A-check maintenance",
                startDate
        );

        ArgumentCaptor<MaintenanceRecord> captor =
                ArgumentCaptor.forClass(MaintenanceRecord.class);
        verify(maintenanceRecordRepository, times(1)).save(captor.capture());

        MaintenanceRecord savedRecord = captor.getValue();
        assertEquals(aircraft, savedRecord.getAircraft());
        assertEquals(template, savedRecord.getMaintenanceTemplate());
        assertEquals(6.5, savedRecord.getDurationHours());
        assertEquals("Scheduled A-check maintenance", savedRecord.getDescription());
        assertEquals(startDate, savedRecord.getStartDate());
        assertEquals(MaintenanceStatus.INLINE, savedRecord.getStatus());
    }

    @Test
    void shouldSaveMaintenanceRecordWhenExecuteWithRecordObject() {
        MaintenanceRecord record = mock(MaintenanceRecord.class);

        useCase.execute(record);

        verify(maintenanceRecordRepository, times(1)).save(record);
    }
}
