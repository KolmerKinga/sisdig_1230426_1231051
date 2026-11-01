package pt.isep.aisafe.maintenance.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import pt.isep.aisafe.maintenance.domain.UsedPart;

/** Body of POST /api/maintenance/records/{id}/parts. */
public record AddPartRequest(
        @NotBlank String partSerialNumber,
        @NotNull @Positive Integer quantity,
        @NotNull @PositiveOrZero Double price) {

    public UsedPart toDomain() {
        return new UsedPart(partSerialNumber, quantity, price);
    }
}
