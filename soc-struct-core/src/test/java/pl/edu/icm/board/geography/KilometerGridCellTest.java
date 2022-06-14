package pl.edu.icm.board.geography;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;
import pl.edu.icm.board.model.Area;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class KilometerGridCellTest {

    @Test
    public void calculateNeighboringChebyshevCircle() {
        // given
        KilometerGridCell kilometerGridCell = new KilometerGridCell(10, 0);

        // execute
        var results = kilometerGridCell.neighboringChebyshevCircle(2).collect(Collectors.toSet());

        // assert
        assertThat(results).hasSize(25);
        for (int x = 8; x <= 12; x++) {
            for (int y = -2; y <= 2; y++) {
                assertThat(results).contains(new KilometerGridCell(x, y));
            }
        }
    }

    @Test
    public void calculateNeighboringCircle() {
        KilometerGridCell kilometerGridCell = new KilometerGridCell(10, 0);
        var r = 100;
        var comparison = kilometerGridCell.neighboringChebyshevCircle(r).collect(Collectors.toSet());
        var results = kilometerGridCell.neighboringCircle(r).collect(Collectors.toSet());

        assertThat((double)comparison.size() / (double)results.size()).isCloseTo(4 / Math.PI, Percentage.withPercentage(1));
    }

    @Test
    void fromArea() {
        //given
        var area = new Area((short) 100, (short) 200);
        //execute
        var kilometerGridCellFromArea = KilometerGridCell.fromArea(area);
        //assert
        assertThat(kilometerGridCellFromArea.getN()).isEqualTo(area.getN());
        assertThat(kilometerGridCellFromArea.getE()).isEqualTo(area.getE());
    }

    @Test
    void toArea() {
        //given
        var kilometerGridCell = new KilometerGridCell(100,200);
        //execute
        var areaFromKilometerGridCell = kilometerGridCell.toArea();
        //assert
        assertThat(areaFromKilometerGridCell.getN()).isEqualTo((short) kilometerGridCell.getN());
        assertThat(areaFromKilometerGridCell.getE()).isEqualTo((short) kilometerGridCell.getE());
    }
}
