package pt.isep.aisafe.aircraft.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class SeatingPack {

    @Column(name = "seating_capacity", nullable = false)
    private Integer seatingCapacity;

    @Column(name = "seating_orientation", nullable = false)
    private String seatingOrientation;

    protected SeatingPack() {}

    public SeatingPack(Integer seatingCapacity, String seatingOrientation) {
        if (seatingCapacity == null || seatingCapacity <= 0) {
            throw new IllegalArgumentException("Seating capacity must be positive");
        }
        if (seatingOrientation == null || seatingOrientation.isBlank()) {
            throw new IllegalArgumentException("Seating orientation cannot be empty");
        }

        this.seatingCapacity = seatingCapacity;
        this.seatingOrientation = seatingOrientation;
    }

    public Integer getSeatingCapacity() {
        return seatingCapacity;
    }

    public String getSeatingOrientation() {
        return seatingOrientation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SeatingPack that = (SeatingPack) o;
        return Objects.equals(seatingCapacity, that.seatingCapacity) &&
                Objects.equals(seatingOrientation, that.seatingOrientation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seatingCapacity, seatingOrientation);
    }
}
