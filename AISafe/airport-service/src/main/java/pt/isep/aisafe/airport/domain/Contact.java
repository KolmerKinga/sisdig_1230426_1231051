package pt.isep.aisafe.airport.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Value object: one contact of an airport (PSOFT US208).
 */
@Embeddable
public class Contact {

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_type", nullable = false)
    private ContactType type;

    @Column(name = "contact_value", nullable = false)
    private String value;

    protected Contact() {
        // for JPA
    }

    public Contact(ContactType type, String value) {
        if (type == null || value == null || value.isBlank()) {
            throw new InvalidAirportException("Contact type and value are required");
        }
        this.type = type;
        this.value = value;
    }

    public ContactType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
