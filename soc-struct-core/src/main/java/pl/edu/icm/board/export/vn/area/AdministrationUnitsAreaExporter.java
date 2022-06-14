package pl.edu.icm.board.export.vn.area;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.commune.Commune;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.trurl.visnow.VnAreaExporter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AdministrationUnitsAreaExporter {
    private final String administrationMapFilename;
    private final CommuneManager communeManager;
    private Map<Commune, Short> communeToShortMap;

    @WithFactory
    public AdministrationUnitsAreaExporter(String administrationMapFilename, CommuneManager communeManager) {
        this.administrationMapFilename = administrationMapFilename;
        this.communeManager = communeManager;
    }

    public void generateReports() throws IOException {
        int height = communeManager.getGridRows();
        int width = communeManager.getGridCols();
        KilometerGridCell cornerA = KilometerGridCell.fromLegacyPdynCoordinates(0, 0);
        KilometerGridCell cornerB = KilometerGridCell.fromLegacyPdynCoordinates(width, height);

        communeToShortMap = communeManager.getCommunes().stream()
                .collect(Collectors.toMap(
                        commune -> commune,
                        commune -> Short.parseShort(commune.getTeryt().substring(0, 2))));
        communeToShortMap.put(
                communeManager.communeAt(KilometerGridCell.fromLegacyPdynCoordinates(0, 0)), (short)0);

        int fromX = Math.min(cornerA.getE(), cornerB.getE());
        int fromY = Math.min(cornerA.getN(), cornerB.getN());

        var exporter = VnAreaExporter.create(
                        AdministrationUnitAreaItem.class,
                        administrationMapFilename,
                        fromX,
                        width,
                        fromY,
                        height);

        var item = new AdministrationUnitAreaItem();

        Short noCommune = 0;

        for (int x = fromX; x < fromX + width; x++) {
            for (int y = fromY; y < fromY + height; y++) {
                Commune commune = communeManager.communeAt(KilometerGridCell.fromPl1992ENKilometers(x, y));
                item.setCommune(communeToShortMap.getOrDefault(commune, noCommune));
                exporter.append(x, y, item);
            }
        }

        exporter.close();
    }
}
