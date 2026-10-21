package pt.isep.aisafe.airport.api;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.stereotype.Component;
import pt.isep.aisafe.airport.api.dto.CreateAirportRequest;
import pt.isep.aisafe.airport.api.dto.CreateAirportRequest.CertificationRequest;
import pt.isep.aisafe.airport.api.dto.CreateAirportRequest.ContactRequest;
import pt.isep.aisafe.airport.api.dto.CreateAirportRequest.FacilitiesRequest;
import pt.isep.aisafe.airport.api.dto.CreateAirportRequest.LocationRequest;
import pt.isep.aisafe.airport.api.dto.CreateAirportRequest.RunwayRequest;
import pt.isep.aisafe.airport.application.InvalidRequestException;
import pt.isep.aisafe.airport.domain.AirportStatus;
import pt.isep.aisafe.airport.domain.ContactType;
import pt.isep.aisafe.airport.domain.RunwayStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Parses the PSOFT airport CSV format (US225), one airport per row, header on the first line:
 *
 * <pre>
 * 0 iata, 1 type, 2 name, 3 status, 4 city, 5 longitude, 6 latitude, 7 region, 8 timezone,
 * 9 country, 10 runwayCount, 11 runways "name|length|surface|status;...", 12 terminalCount,
 * 13 services "a;b", 14 gateCount, 15 certification names "a;b", 16 start dates "yyyy-mm-dd;...",
 * 17 expiry dates "yyyy-mm-dd;...", 18 phone, 19 email, 20 photos "a;b"
 * </pre>
 *
 * Rows that cannot be parsed are reported, not silently dropped.
 */
@Component
public class AirportCsvParser {

    private static final int COLUMNS = 21;

    public record ParsedRow(int line, CreateAirportRequest request, String error) {
    }

    public List<ParsedRow> parse(InputStream input) throws IOException {
        List<ParsedRow> rows = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String[] row;
            int line = 0;
            while ((row = reader.readNext()) != null) {
                line++;
                if (line == 1 && row.length > 0 && row[0].trim().equalsIgnoreCase("iata")) {
                    continue; // header
                }
                if (row.length == 1 && row[0].isBlank()) {
                    continue; // empty line
                }
                try {
                    rows.add(new ParsedRow(line, toRequest(row), null));
                } catch (RuntimeException e) {
                    rows.add(new ParsedRow(line, null, e.getMessage()));
                }
            }
        } catch (CsvValidationException e) {
            throw new InvalidRequestException("Invalid CSV file: " + e.getMessage());
        }
        return rows;
    }

    private CreateAirportRequest toRequest(String[] c) {
        if (c.length < COLUMNS) {
            throw new IllegalArgumentException("expected " + COLUMNS + " columns but found " + c.length);
        }
        List<String> certNames = split(c[15]);
        List<String> certStarts = split(c[16]);
        List<String> certExpires = split(c[17]);
        List<CertificationRequest> certifications = new ArrayList<>();
        for (int i = 0; i < certNames.size(); i++) {
            LocalDate start = i < certStarts.size() ? LocalDate.parse(certStarts.get(i)) : LocalDate.now();
            LocalDate end = i < certExpires.size() ? LocalDate.parse(certExpires.get(i)) : start.plusYears(1);
            certifications.add(new CertificationRequest(certNames.get(i), "STANDARD", start, end));
        }

        List<ContactRequest> contacts = new ArrayList<>();
        if (!c[18].isBlank()) {
            contacts.add(new ContactRequest(ContactType.PHONE, c[18].trim()));
        }
        if (!c[19].isBlank()) {
            contacts.add(new ContactRequest(ContactType.EMAIL, c[19].trim()));
        }

        return new CreateAirportRequest(
                c[0].trim().toUpperCase(Locale.ROOT),
                c[1].trim(),
                c[2].trim(),
                AirportStatus.valueOf(c[3].trim().toUpperCase(Locale.ROOT)),
                null,
                new LocationRequest(c[4].trim(), c[9].trim(), c[7].trim(), c[8].trim(),
                        Double.parseDouble(c[6].trim()), Double.parseDouble(c[5].trim())),
                new FacilitiesRequest(Integer.parseInt(c[12].trim()), Integer.parseInt(c[14].trim()), split(c[13])),
                parseRunways(c[11]),
                certifications,
                contacts,
                split(c[20]));
    }

    private List<RunwayRequest> parseRunways(String raw) {
        return split(raw).stream()
                .map(part -> part.split("\\|"))
                .map(p -> {
                    if (p.length < 4) {
                        throw new IllegalArgumentException("runway must be name|length|surface|status");
                    }
                    return new RunwayRequest(p[0].trim(), Double.parseDouble(p[1].trim()), p[2].trim(),
                            RunwayStatus.valueOf(p[3].trim().toUpperCase(Locale.ROOT)));
                })
                .toList();
    }

    private static List<String> split(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
