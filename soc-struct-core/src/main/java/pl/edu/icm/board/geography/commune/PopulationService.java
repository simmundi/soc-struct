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
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.density.PopulationDensityLoader;
import pl.edu.icm.em.socstruct.component.Household;
import pl.edu.icm.em.socstruct.component.geo.Location;
import pl.edu.icm.trurl.ecs.util.ActionService;
import pl.edu.icm.trurl.ecs.util.Indexes;
import pl.edu.icm.trurl.ecs.util.IteratingStepBuilder;
import pl.edu.icm.trurl.util.Status;

import java.util.HashMap;
import java.util.Map;

;

public class PopulationService {
    private final Map<String, Integer> populationByTeryt = new HashMap<>();
    private final PopulationDensityLoader populationDensityLoader;
    private final CommuneManager communeManager;
    private final EngineIo engineIo;
    private final ActionService actionService;
    private final Indexes indexes;

    @WithFactory
    public PopulationService(EngineIo engineIo,
                             CommuneManager communeManager,
                             PopulationDensityLoader populationDensityLoader,
                             ActionService actionService,
                             Indexes indexes) {
        this.populationDensityLoader = populationDensityLoader;
        this.communeManager = communeManager;
        this.engineIo = engineIo;
        this.actionService = actionService;
        this.indexes = indexes;
    }

    public void load() {
        if (!populationByTeryt.isEmpty()) {
            return;
        }
        populationDensityLoader.loadActualPopulationFromEngine();
        var status = Status.of("loading population", 1_000_000);
        engineIo.getEngine().execute(IteratingStepBuilder.iteratingOver(indexes.allWithComponents(Household.class, Location.class))
                .withoutContext().perform(actionService.withComponents(Household.class, Location.class, (entity, household, location) -> {
                    var teryt = communeManager.communeAt(KilometerGridCell.fromLocation(location)).getTeryt();
                    var size = household.getMembers().size();
                    if (isACityWithPowiatRights(teryt)) {
                        teryt = teryt.substring(0, 4);
                    } else {
                        teryt = teryt.substring(0, 6);
                    }
                    populationByTeryt.compute(teryt, (t, v) -> (v == null) ? size : v + size);
                    status.tick();
                })).build());
        status.done();
    }

    public AdministrationAreaType typeFromLocation(Location location) {
        var cell = KilometerGridCell.fromLocation(location);
        var teryt = communeManager.communeAt(cell).getTeryt();
        var areaCode = teryt.substring(6);
        switch (areaCode) {
            case "2":
            case "5":
                return AdministrationAreaType.VILLAGE;
            case "1":
            case "4":
            case "8":
            case "9":
                if (isACityWithPowiatRights(teryt)) {
                    return AdministrationAreaType.fromCityPopulation(populationByTeryt.get(teryt.substring(0, 4)));
                }
                return AdministrationAreaType.fromCityPopulation(populationByTeryt.get(teryt.substring(0, 6)));
            default:
                if (isACityWithPowiatRights(teryt)) {
                    return AdministrationAreaType.fromCityPopulation(populationByTeryt.get(teryt.substring(0, 4)));
                }
            case "3":
                if (populationDensityLoader.density(cell) > 1000) {
                    return AdministrationAreaType.fromCityPopulation(populationByTeryt.get(teryt.substring(0, 6)));
                } else return AdministrationAreaType.VILLAGE;
        }
    }

    public boolean isACityWithPowiatRights(String teryt) {
        var terytPowiatu = Integer.parseInt(teryt.substring(2, 4));
        return (terytPowiatu >= 61 && terytPowiatu <= 99);
    }
}
