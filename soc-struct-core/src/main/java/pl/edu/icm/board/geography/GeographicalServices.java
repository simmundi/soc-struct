package pl.edu.icm.board.geography;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.model.Area;

public class GeographicalServices {

    @WithFactory
    GeographicalServices(){}

    public KilometerGridCell convertAreaToKilometerGridCell(Area area){
        return KilometerGridCell.fromArea(area);
    }
}
