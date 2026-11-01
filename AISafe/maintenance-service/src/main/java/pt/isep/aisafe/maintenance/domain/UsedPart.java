package pt.isep.aisafe.maintenance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Part used in a maintenance (PSOFT US220 costs, US226 inventory).
 */
@Embeddable
public class UsedPart {

    @Column(nullable = false)
    private String partSerialNumber;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double price;

    protected UsedPart() {
        // for JPA
    }

    public UsedPart(String partSerialNumber, Integer quantity, Double price) {
        if (partSerialNumber == null || partSerialNumber.isBlank()) {
            throw new InvalidMaintenanceException("Part serial number is required");
        }
        if (quantity == null || quantity <= 0) {
            throw new InvalidMaintenanceException("Part quantity must be positive");
        }
        if (price == null || price < 0) {
            throw new InvalidMaintenanceException("Part price cannot be negative");
        }
        this.partSerialNumber = partSerialNumber.trim();
        this.quantity = quantity;
        this.price = price;
    }

    public double cost() {
        return quantity * price;
    }

    public String getPartSerialNumber() {
        return partSerialNumber;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Double getPrice() {
        return price;
    }
}
