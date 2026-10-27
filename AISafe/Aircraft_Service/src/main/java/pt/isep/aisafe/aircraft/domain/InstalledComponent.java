package pt.isep.aisafe.aircraft.domain;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "installed_components")
public class InstalledComponent {

    @Id
    @Column(name = "serial_number", nullable = false, updatable = false)
    private String serialNumber;

    @Column(name = "part_number", nullable = false)
    private String partNumber;

    @Column(nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aircraft_registration_number", nullable = false)
    private Aircraft aircraft;

    protected InstalledComponent() {}

    public InstalledComponent(String serialNumber, String partNumber, String status, Aircraft aircraft) {
        if (serialNumber == null || serialNumber.isBlank()) {
            throw new IllegalArgumentException("Serial number cannot be empty");
        }
        if (partNumber == null || partNumber.isBlank()) {
            throw new IllegalArgumentException("Part number cannot be empty");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }

        this.serialNumber = serialNumber;
        this.partNumber = partNumber;
        this.status = status;
        this.aircraft = aircraft;
    }

    public void updateStatus(String newStatus) {
        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }
        this.status = newStatus;
    }

    public void updatePartNumber(String newPartNumber) {
        if (newPartNumber == null || newPartNumber.isBlank()) {
            throw new IllegalArgumentException("Part number cannot be empty");
        }
        this.partNumber = newPartNumber;
    }

    public void setAircraft(Aircraft aircraft) {
        this.aircraft = aircraft;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public String getStatus() {
        return status;
    }

    public Aircraft getAircraft() {
        return aircraft;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstalledComponent that = (InstalledComponent) o;
        return Objects.equals(serialNumber, that.serialNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serialNumber);
    }
}
