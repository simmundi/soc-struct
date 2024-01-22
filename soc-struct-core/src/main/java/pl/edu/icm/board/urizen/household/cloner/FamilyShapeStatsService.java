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

package pl.edu.icm.board.urizen.household.cloner;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.agesex.AgeSexFromDistributionPicker;
import pl.edu.icm.em.common.detached.DetachedEntityStreamer;
import pl.edu.icm.em.common.math.histogram.Bin;
import pl.edu.icm.em.common.math.histogram.Histogram;
import pl.edu.icm.em.socstruct.component.Household;
import pl.edu.icm.em.socstruct.component.NameTag;
import pl.edu.icm.em.socstruct.component.Person;
import pl.edu.icm.em.socstruct.component.geo.AdministrationUnitTag;
import pl.edu.icm.trurl.util.Status;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Services which counts statistics for the current engine state.
 * <p>
 * These include:
 * <ul>
 *  <li>count of families per teryt, binned by histogram of member by age
 *  <li>count of people per teryt</li>
 * </ul>
 */
public class FamilyShapeStatsService {
    private final EngineIo engineIo;
    private final DetachedEntityStreamer entityStreamer;
    private final AgeSexFromDistributionPicker ageSexFromDistributionPicker;

    @WithFactory
    public FamilyShapeStatsService(EngineIo engineIo, DetachedEntityStreamer entityStreamer, AgeSexFromDistributionPicker ageSexFromDistributionPicker) {
        this.engineIo = engineIo;
        this.entityStreamer = entityStreamer;
        this.ageSexFromDistributionPicker = ageSexFromDistributionPicker;
        engineIo.require(
                Household.class,
                AdministrationUnitTag.class,
                Person.class,
                NameTag.class);
    }

    public FamilyShapeStats countStats() {
        FamilyShapeStats familyShapeStats = new FamilyShapeStats();
        var status = Status.of("Finding family statistics", 1_000_000);
        Map<HouseholdShape, Bin> shapes = new HashMap<>();
        entityStreamer
                .streamDetached()
                .map(e -> HouseholdShape.tryCreate(e, ageSexFromDistributionPicker))
                .filter(shape -> shape != null)
                .forEach(shape -> {
                    status.tick();
                    familyShapeStats.populationByTeryt.computeIfAbsent(shape.getTeryt(), t -> new AtomicInteger())
                            .addAndGet(shape.getMemberCount());
                    shapes
                            .computeIfAbsent(shape, s -> familyShapeStats.shapesByTeryt
                                    .computeIfAbsent(s.getTeryt(), t -> new Histogram<>())
                                    .add(s, 0))
                            .add(1);
                });
        status.done();
        return familyShapeStats;
    }
}
