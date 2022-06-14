package pl.edu.icm.board.geography;

import org.junit.jupiter.api.Test;
import pl.edu.icm.board.model.Area;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class GeographicalServicesTest {

    @Test
    void convertAreaToKilometerGridCell() {
        //given
        var geographicalServices = new GeographicalServices();
        var area = new Area((short) 100, (short) 150);
        //execute
        var kgc = geographicalServices.convertAreaToKilometerGridCell(area);
        //assert
        assertThat(kgc).isEqualTo(KilometerGridCell.fromArea(area));
    }
}