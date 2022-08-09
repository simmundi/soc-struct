package pl.edu.icm.board.geography.gis;

import net.snowyhollows.bento.annotation.WithFactory;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.util.Status;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Stateless, cacheless source of data about geography and administration units.
 *
 */
public class CommuneSource {
    public static final String NULL_TERYT = "0000000";
    public static final String NULL_NAME = "leones";
    public static final String TERYT_ATTRIBUTE_NAME = "JPT_KOD_JE";
    public static final String NAZWA_ATTRIBUTE_NAME = "JPT_NAZWA_";

    private final GisUtilsService gisUtilsService;
    private final CommuneGisReader communeGisReader;
    private final int gridRows;
    private final int gridCols;

    @WithFactory
    public CommuneSource(GisUtilsService gisUtilsService,
                         CommuneGisReader communeGisReader,
                         int gridColumns,
                         int gridRows) {
        this.gisUtilsService = gisUtilsService;
        this.communeGisReader = communeGisReader;
        this.gridCols = gridColumns;
        this.gridRows = gridRows;
    }

    public void load(Store store) {
        String[][] terytGrid = new String[gridRows][gridCols];
        Map<String, String> terytToNameMap = new HashMap<>();

        processGisData(terytGrid, terytToNameMap);

        Mapper<CommuneStoreItem> gridItemMapper = Mappers.create(CommuneStoreItem.class);
        gridItemMapper.configureStore(store);
        gridItemMapper.attachStore(store);

        CommuneStoreItem item = gridItemMapper.create();

        int idx = 0;
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                var cell = KilometerGridCell.fromLegacyPdynCoordinates(col, row);
                item.setE(cell.getE());
                item.setN(cell.getN());
                item.setName(terytToNameMap.get(terytGrid[row][col]));
                item.setTeryt(terytGrid[row][col]);
                gridItemMapper.save(item, idx++);
            }
        }

        store.fireUnderlyingDataChanged(0, idx);
    }

    public int getGridRows() {
        return gridRows;
    }

    public int getGridCols() {
        return gridCols;
    }

    private void processGisData(String[][] terytGrid, Map<String, String> terytToNameMap) {
        try {
            SimpleFeatureSource features = communeGisReader.communes();
            KilometerGridCell.fromLegacyPdynCoordinates(0, 0);

            var status = Status.of("Slicing map into kilometer grid", Math.max(gridCols * gridCols / 30, 1));

            for (int row = 0; row < gridRows; row++) {
                for (int col = 0; col < gridCols; col++) {

                    status.tick();
                    FeaturesInEnvelope featuresInEnvelope =
                            gisUtilsService.findFeaturesInCell(features, KilometerGridCell.fromLegacyPdynCoordinates(col, row));
                    if (featuresInEnvelope.isEmpty()) {
                        terytGrid[row][col] = NULL_TERYT;
                        terytToNameMap.put(NULL_TERYT, NULL_NAME);
                    } else {
                        SimpleFeature featureWithMostArea = featuresInEnvelope
                                .getFeatureWithMostArea();

                        String featureTeryt = (String) featureWithMostArea.getAttribute(TERYT_ATTRIBUTE_NAME);
                        String featureName = (String) featureWithMostArea.getAttribute(NAZWA_ATTRIBUTE_NAME);
                        terytGrid[row][col] = featureTeryt;
                        terytToNameMap.put(featureTeryt, featureName);
                    }
                }
            }
            status.done();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
