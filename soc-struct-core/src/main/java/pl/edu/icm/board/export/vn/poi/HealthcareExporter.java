package pl.edu.icm.board.export.vn.poi;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.model.*;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class HealthcareExporter {

    private final Board board;
    private final PoiExporter poiExporter;
    private final String healthcareExportFilename;

    private final static Map<HealthcareType, PoiItem.Type> levelsToTypes = new EnumMap<>(Map.of(
            HealthcareType.POZ, PoiItem.Type.HEALTHCARE_POZ,
            HealthcareType.OTHER, PoiItem.Type.HEALTHCARE_OTHER
    ));

    @WithFactory
    public HealthcareExporter(Board board, PoiExporter poiExporter, String healthcareExportFilename) {
        this.board = board;
        this.poiExporter = poiExporter;
        this.healthcareExportFilename = healthcareExportFilename;

        board.require(Location.class, Healthcare.class);
    }

    public void export() throws IOException {
        var engine = board.getEngine();
        poiExporter.export(healthcareExportFilename,
                engine
                        .streamDetached()
                        .filter(e -> e.optional(Healthcare.class).isPresent()),
                (poiItem, entity) -> {
                    entity.optional(Healthcare.class).ifPresent(ei -> {
                        poiItem.setSubsets(levelsToTypes.get(ei.getType()));
                    });
                    return poiItem;
                });
    }
}
