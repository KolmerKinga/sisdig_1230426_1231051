package pt.isep.aisafe.airport.api;

import org.junit.jupiter.api.Test;
import pt.isep.aisafe.airport.domain.AirportStatus;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AirportCsvParserTest {

    private static final String HEADER = "iata,type,name,status,city,longitude,latitude,region,timezone,country,"
            + "runwayCount,runways,terminalCount,services,gateCount,certifications,certStarts,certExpires,"
            + "phone,email,photos\n";

    private final AirportCsvParser parser = new AirportCsvParser();

    private List<AirportCsvParser.ParsedRow> parse(String csv) throws Exception {
        return parser.parse(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void parsesValidRow() throws Exception {
        String csv = HEADER + "LHR,International,Heathrow Airport,OPERATIONAL,London,-0.4543,51.4700,"
                + "Greater London,Europe/London,United Kingdom,2,"
                + "\"09L/27R|3902|Asphalt|IN_USE;09R/27L|3902|Asphalt|IN_USE\",5,"
                + "\"Lounges;Duty Free\",80,\"ICAO Annex 14 Compliance\",\"2022-01-01\",\"2027-01-01\","
                + "\"+44 844 335 1801\",\"info@heathrow.com\",\"lhr.jpg\"\n";

        List<AirportCsvParser.ParsedRow> rows = parse(csv);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).error()).isNull();
        assertThat(rows.get(0).request().iataCode()).isEqualTo("LHR");
        assertThat(rows.get(0).request().status()).isEqualTo(AirportStatus.OPERATIONAL);
        assertThat(rows.get(0).request().runways()).hasSize(2);
        assertThat(rows.get(0).request().location().latitude()).isEqualTo(51.47);
        assertThat(rows.get(0).request().contacts()).hasSize(2);
    }

    @Test
    void reportsInvalidRowInsteadOfDroppingIt() throws Exception {
        String csv = HEADER + "XXX,International,Broken,NOT_A_STATUS,City,0,0,R,UTC,C,0,,1,,1,,,,,,\n";

        List<AirportCsvParser.ParsedRow> rows = parse(csv);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).request()).isNull();
        assertThat(rows.get(0).error()).isNotBlank();
    }
}
