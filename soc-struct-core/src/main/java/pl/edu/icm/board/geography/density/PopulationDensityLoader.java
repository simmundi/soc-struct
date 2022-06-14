package pl.edu.icm.board.geography.density;

import com.univocity.parsers.common.record.Record;
import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.util.BoardCsvLoader;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.util.EntityIterator;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.util.Status;

import java.io.FileNotFoundException;
import java.util.*;

/**
 * PopulationDensityLoader loads and provides real world information about
 * population (density, makeup etc.), in kilometer grid.
 * <p>
 * This is NOT based on the current state of the engine, but rather
 * statistical data (e.g. files from GUS).
 * <p>
 * It is assumed that it is accurate at least for Poland.
 */
public class PopulationDensityLoader {

    private final BoardCsvLoader boardCsvLoader;
    private final String gmPopulationGridFilename;
    private final List<KilometerGridCell> cellList = new ArrayList<>();
    private final Set<KilometerGridCell> cellSet = new HashSet<>();
    private final Map<KilometerGridCell, Integer> densityMap = new HashMap<>();
    private final Board board;
    private final Selectors selectors;

    @WithFactory
    public PopulationDensityLoader(BoardCsvLoader boardCsvLoader, String gmPopulationGridFilename, Board board, Selectors selectors) {
        this.boardCsvLoader = boardCsvLoader;
        this.gmPopulationGridFilename = gmPopulationGridFilename;
        this.board = board;
        this.selectors = selectors;
    }

    public void load() throws FileNotFoundException {
        if (!cellList.isEmpty()) {
            return;
        }
        var status = Status.of("loading population density from file", 10000);
        for (Record record : boardCsvLoader.stream(gmPopulationGridFilename)) {
            String idOczka = record.getString("id_oczka");
            KilometerGridCell cell = KilometerGridCell.fromIdOczkaGus(idOczka);
            cellList.add(cell);
            cellSet.add(cell);
            status.tick();
        }
        status.done();
    }

    public void loadActualPopulationFromEngine() {
        if (!cellList.isEmpty()) {
            cellList.clear();
            cellSet.clear();
        }
        Engine engine = board.getEngine();
        var status = Status.of("loading population density from engine", 500000);
        engine.execute(EntityIterator.select(selectors.allWithComponents(Household.class, Location.class)).dontPersist().forEach(Household.class, Location.class, (entity, members, location) -> {
            var size = members.getMembers().size();
            KilometerGridCell cell = KilometerGridCell.fromLocation(location);
            cellSet.add(cell);
            densityMap.compute(cell, (c,v) -> (v == null) ? size : v + size);
            status.tick();
        }));
        cellList.addAll(cellSet);
        status.done();
    }

    public KilometerGridCell sample(double random) {
        return cellList.get((int) (cellList.size() * random));
    }

    public int density(KilometerGridCell cell) {
        return densityMap.get(cell);
    }

    public boolean isPopulated(KilometerGridCell kilometerGridCell) {
        return cellSet.contains(kilometerGridCell);
    }
}
