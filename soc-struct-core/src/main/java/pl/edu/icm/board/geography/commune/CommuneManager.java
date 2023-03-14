/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.board.geography.commune;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.gis.CommuneSource;
import pl.edu.icm.board.geography.gis.CommuneStoreItem;
import pl.edu.icm.board.model.Location;
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
        Store store = new ArrayStore(1024);
        var gridItemMapper = new Mappers().create(CommuneStoreItem.class);
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
                .values().forEach(cells -> {
                            var commune = new Commune(cells.get(0).getTeryt(), cells.get(0).getName(), cells.stream().map(
                                    cell -> KilometerGridCell.fromPl1992ENKilometers(cell.getE(), cell.getN())
                            ).collect(Collectors.toSet()));
                            commune.getCells()
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

    public Commune communeAt(Location location) {
        return communeAt(KilometerGridCell.fromLocation(location));
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

        // Teryt częściowy do terytu gminy (i.e. tylko części miejskiej lub tylko wiejskiej)
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
