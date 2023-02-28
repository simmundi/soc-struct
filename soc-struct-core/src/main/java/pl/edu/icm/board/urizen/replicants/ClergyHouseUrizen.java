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

package pl.edu.icm.board.urizen.replicants;

import net.snowyhollows.bento.annotation.WithFactory;
import org.apache.commons.math3.random.RandomDataGenerator;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.agesex.AgeSexFromDistributionPicker;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.geography.density.PopulationDensityLoader;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Replicant;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.household.model.AgeRange;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.bin.BinPool;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;

import java.io.FileNotFoundException;
import java.util.List;

public class ClergyHouseUrizen {
    private final EngineIo engineIo;
    private final PopulationDensityLoader populationDensityLoader;
    private final ReplicantPrototypes prototypes;
    private final RandomDataGenerator random;
    private final int clergyHouseReplicantsCount;
    private final int clergyHouseRoomSize;
    private final BinPool<AgeRange> ages;
    private final BinPool<Person.Sex> sexes;
    private final AgeSexFromDistributionPicker ageSexFromDistributionPicker;

    @WithFactory
    public ClergyHouseUrizen(
            EngineIo engineIo,
            ReplicantPrototypes prototypes,
            ReplicantsPopulation replicantsPopulation,
            PopulationDensityLoader populationDensityLoader,
            AgeSexFromDistributionPicker ageSexFromDistributionPicker, int clergyHouseReplicantsCount, int clergyHouseRoomSize, RandomProvider randomProvider) {
        this.engineIo = engineIo;
        this.prototypes = prototypes;
        this.populationDensityLoader = populationDensityLoader;
        this.ageSexFromDistributionPicker = ageSexFromDistributionPicker;
        this.clergyHouseReplicantsCount = clergyHouseReplicantsCount;
        this.clergyHouseRoomSize = clergyHouseRoomSize;
        this.engineIo.require(Household.class, Person.class, Location.class, Replicant.class);
        this.sexes = replicantsPopulation.getPopulation().getPeopleBySex().createSubPool(Person.Sex.M);
        this.ages = replicantsPopulation.getPopulation().getPeopleByAge().createSubPool(
                AgeRange.AGE_25_29,
                AgeRange.AGE_30_34,
                AgeRange.AGE_35_39,
                AgeRange.AGE_40_44,
                AgeRange.AGE_45_49,
                AgeRange.AGE_50_54,
                AgeRange.AGE_55_59,
                AgeRange.AGE_60_64,
                AgeRange.AGE_65_69,
                AgeRange.AGE_70_74,
                AgeRange.AGE_75_79,
                AgeRange.AGE_80_
        );
        this.random = randomProvider.getRandomDataGenerator(ClergyHouseUrizen.class);
    }

    public void fabricate() throws FileNotFoundException {
        populationDensityLoader.load();
        int maxSize = clergyHouseRoomSize;
        int minSize = 1;
        int count = clergyHouseReplicantsCount;
        int size;
        while (count > 0) {
            size = Math.min(random.nextInt(minSize, maxSize), count);
            generateClergyHouse(size);
            count -= 1;
        }
    }

    private void generateClergyHouse(int roomSize) {
        KilometerGridCell cell = populationDensityLoader.sample(random.getRandomGenerator().nextDouble());
        generateRoom(roomSize, cell);
    }

    private void generateRoom(int inhabitants, KilometerGridCell cell) {
        engineIo.getEngine().execute(sessionFactory -> {
            Session session = sessionFactory.create();
            List<Entity> dependents = prototypes.clergyHouseRoom(session, cell).get(Household.class).getMembers();
            for (int i = 0; i < inhabitants; i++) {
                var sexPicked = sexes.sample(random.getRandomGenerator().nextDouble()).pick();
                var ageRangePicked = ages.sample(random.getRandomGenerator().nextDouble())
                        .pick();
                dependents.add(prototypes.clergyHouseResident(
                        session,
                        sexPicked,
                        ageSexFromDistributionPicker.getEmpiricalDistributedRandomAge(sexPicked,
                                ageRangePicked, random.getRandomGenerator().nextDouble())));
            }
            session.close();
        });
    }
}
