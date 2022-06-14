package pl.edu.icm.board.geography.commune;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.gis.CommuneSource;
import pl.edu.icm.board.geography.gis.CommuneStoreItem;
import pl.edu.icm.board.util.FileCacheService;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class CommuneManager {
    private final Commune nowhere;
    private final Map<String, Commune> communesByTeryt = new HashMap<>();
    private final Commune[] grid;
    private final int gridCols;
    private final int gridRows;

    @WithFactory
    public CommuneManager(FileCacheService fileCacheService, CommuneSource communeSource) {
        Store store = new ArrayStore();
        var gridItemMapper = Mappers.create(CommuneStoreItem.class);
        gridItemMapper.configureStore(store);
        fileCacheService
                .computeIfAbsent("gminy", store, communeSource::load);

        gridItemMapper.attachStore(store);

        gridRows = communeSource.getGridRows();
        gridCols = communeSource.getGridCols();
        grid = new Commune[gridRows * gridCols];

        Mappers.stream(gridItemMapper)
                .collect(groupingBy(
                        CommuneStoreItem::getTeryt,
                        Collectors.toList()))
                .values()
                .stream().forEach(cells -> {
                            var commune = new Commune(cells.get(0).getTeryt(), cells.get(0).getName(), cells.stream().map(
                                    cell -> KilometerGridCell.fromPl1992ENKilometers(cell.getE(), cell.getN())
                            ).collect(Collectors.toSet()));
                            commune.getCells().stream()
                                    .forEach(cell -> setCommuneAt(cell, commune));
                            communesByTeryt.put(commune.getTeryt(), commune);
                        }
                );

        nowhere = new Commune("0000000", "leones");
    }

    public Commune communeAt(KilometerGridCell cell) {
        int col = cell.getLegacyPdynCol();
        int row = cell.getLegacyPdynRow();
        return isWithinGrid(col, row) ? grid[communeIndex(col, row)] : nowhere;
    }

    public Optional<Commune> communeForTeryt(String teryt) {
        return Optional.ofNullable(communesByTeryt.get(fixTeryt(teryt)));
    }

    public Collection<Commune> getCommunes() {
        return communesByTeryt.values();
    }

    public int getGridCols() {
        return gridCols;
    }

    public int getGridRows() {
        return gridRows;
    }

    private String fixTeryt(String teryt) {

        // ciekawa historia, wybrałem większą: https://pl.wikipedia.org/wiki/Ostrowice_(gmina)
        if (teryt.equals("3203042")) {
            return fixTeryt("3203063");
        }

        // co roku kilka gmin zmienia się z wiejskich w miejskie i zmienia ostatnią cyfrę z 2 na 3
        if (!communesByTeryt.containsKey(teryt) && teryt.endsWith("2")) {
            return fixTeryt(String.format("%.6s3", teryt));
        }

        // teryt częściowy do terytu gminy (i.e. tylko części miejskiej lub tylko wiejskiej)
        if (teryt.endsWith("5") || teryt.endsWith("4")) {
            return fixTeryt(String.format("%.6s3", teryt));
        }

        return teryt;
    }

    private void setCommuneAt(KilometerGridCell location, Commune commune) {
        int index = communeIndex(location.getLegacyPdynCol(), location.getLegacyPdynRow());
        grid[index] = commune;
    }

    private boolean isWithinGrid(int col, int row) {
        return row >= 0 && row < gridRows && col >= 0 && col < gridCols;
    }

    private int communeIndex(int col, int row) {
        return row * gridCols + col;
    }
}
