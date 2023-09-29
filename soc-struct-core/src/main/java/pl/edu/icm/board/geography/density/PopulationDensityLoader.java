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

package pl.edu.icm.board.geography.density;

import com.univocity.parsers.common.record.Record;
import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.EngineIo;
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
    private final EngineIo engineIo;
    private final Selectors selectors;

    @WithFactory
    public PopulationDensityLoader(BoardCsvLoader boardCsvLoader,
                                   @ByName("soc-struct.population.grid.source") String gmPopulationGridFilename,
                                   EngineIo engineIo,
                                   Selectors selectors) {
        this.boardCsvLoader = boardCsvLoader;
        this.gmPopulationGridFilename = gmPopulationGridFilename;
        this.engineIo = engineIo;
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
        Engine engine = engineIo.getEngine();
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
