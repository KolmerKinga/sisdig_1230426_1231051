package pt.isep.aisafe.airport.api.dto;

import jakarta.validation.Valid;
import pt.isep.aisafe.airport.api.dto.CreateAirportRequest.ContactRequest;
import pt.isep.aisafe.airport.domain.Contact;

import java.util.List;

/**
 * Body of PATCH /api/airports/{iataCode}/details (PSOFT US208).
 * Null fields are left unchanged.
 */
public record UpdateDetailsRequest(
        String name,
        String operationalHours,
        List<@Valid ContactRequest> contacts) {

    public List<Contact> contactsAsDomain() {
        return contacts == null ? null
                : contacts.stream().map(ContactRequest::toDomain).toList();
    }
}
