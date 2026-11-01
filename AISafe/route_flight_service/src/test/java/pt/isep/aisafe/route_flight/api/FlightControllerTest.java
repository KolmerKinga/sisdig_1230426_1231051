package pt.isep.aisafe.route_flight.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pt.isep.aisafe.route_flight.application.FlightSearchService;
import pt.isep.aisafe.route_flight.application.FlightService;
import pt.isep.aisafe.route_flight.application.FlightUtilizationReportService;
import pt.isep.aisafe.route_flight.shared.security.SecurityConfig;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FlightController.class)
@Import(SecurityConfig.class)
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlightService flightService;

    @MockBean
    private FlightSearchService flightSearchService;

    @MockBean
    private FlightUtilizationReportService reportService;

    @Test
    @WithMockUser
    void shouldReturnEmptyListWhenNoFlightsExist() throws Exception {
        when(flightSearchService.findAllFlights()).thenReturn(List.of());

        mockMvc.perform(get("/api/flights")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
