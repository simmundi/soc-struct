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
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.urizen.population.trusted.CountyPopulationLoader;
import pl.edu.icm.board.urizen.replicants.ReplicantsCounter;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.em.common.math.histogram.Histogram;
import pl.edu.icm.em.socstruct.component.Household;
import pl.edu.icm.em.socstruct.component.NameTag;
import pl.edu.icm.em.socstruct.component.Person;
import pl.edu.icm.em.socstruct.component.geo.AdministrationUnitTag;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.util.Status;

import java.util.concurrent.atomic.AtomicInteger;

public class HouseholdClonerUrizen {
    private final EngineIo engineIo;
    private final CountyPopulationLoader countyPopulationLoader;
    private final RandomGenerator randomGenerator;
    private final FamilyShapeStatsService familyShapeStatsService;
    private final ReplicantsCounter replicantsCounter;

    @WithFactory
    public HouseholdClonerUrizen(
            EngineIo engineIo,
            CountyPopulationLoader countyPopulationLoader,
            RandomProvider randomProvider,
            FamilyShapeStatsService familyShapeStatsService,
            ReplicantsCounter replicantsCounter) {
        this.countyPopulationLoader = countyPopulationLoader;
        this.engineIo = engineIo;
        this.randomGenerator = randomProvider.getRandomGenerator(HouseholdClonerUrizen.class);
        this.familyShapeStatsService = familyShapeStatsService;
        this.replicantsCounter = replicantsCounter;
        engineIo.require(
                Household.class,
                AdministrationUnitTag.class,
                Person.class,
                NameTag.class);
    }

    public void cloneHouseholds() {
        var familyShapeStats = familyShapeStatsService.countStats();
        var householdStatus = Status.of("Cloning households to scale", 100_000);
        double total = countyPopulationLoader.totalPopulation();
        double replicants = replicantsCounter.getReplicantsCount();;
        double nonReplicantRatio = (total - replicants) / total;
        AtomicInteger peopleCreated = new AtomicInteger();
        AtomicInteger householdsCreated = new AtomicInteger();
        familyShapeStats.populationByTeryt.entrySet().forEach(entry -> {
            String teryt = entry.getKey();
            int terytTotalPopulation = countyPopulationLoader.populationOf(teryt);
            int targetNonReplicantPopulation = (int) (terytTotalPopulation * nonReplicantRatio);
            int householdMembersCount = entry.getValue().get();
            int householdMembersToCreate = targetNonReplicantPopulation - householdMembersCount;
            Histogram<HouseholdShape> householdShapes = familyShapeStats.shapesByTeryt.get(teryt);
            engineIo.getEngine().execute(sessionFactory -> {
                Session session = sessionFactory.createOrGet();
                int counter = householdMembersToCreate;
                while (counter > 0) {
                    HouseholdShape shape = householdShapes.sample(randomGenerator.nextDouble()).pick();
                    int memberCount = shape.getMemberCount();

                    shape.createHouse(session, randomGenerator);
                    counter -= memberCount;
                    peopleCreated.addAndGet(memberCount);
                    householdsCreated.incrementAndGet();
                    householdStatus.tick();
                }
            });
        });
        householdStatus.done("Created %d new household members in %d households; targeting population of %d, including %d replicants", peopleCreated.get(), householdsCreated.get(), (int)total, (int)replicants);
    }
}
