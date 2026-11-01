package pt.isep.aisafe.maintenance.application;

import org.junit.jupiter.api.Test;
import pt.isep.aisafe.maintenance.domain.MaintenanceStatus;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecordFilterTest {

    @Test
    void buildsQueryStringSentToPeers() {
        RecordFilter filter = new RecordFilter(" cs-tma ", null, MaintenanceStatus.ONGOING,
                LocalDate.of(2026, 1, 1), null);

        assertThat(filter.toQueryString()).isEqualTo("aircraft=CS-TMA&status=ONGOING&initialDate=2026-01-01");
        assertThat(RecordFilter.ALL.toQueryString()).isEqualTo("");
    }

    @Test
    void rejectsInvertedDateRange() {
        assertThatThrownBy(() -> new RecordFilter(null, null, null, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1)))
                .isInstanceOf(InvalidRequestException.class);
    }
}
