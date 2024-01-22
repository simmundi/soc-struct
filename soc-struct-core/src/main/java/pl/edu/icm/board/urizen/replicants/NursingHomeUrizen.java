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

import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;
import org.apache.commons.math3.random.RandomDataGenerator;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.agesex.AgeSexFromDistributionPicker;
import pl.edu.icm.em.socstruct.component.geo.Complex;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.em.socstruct.component.geo.Location;
import pl.edu.icm.board.geography.density.PopulationDensityLoader;
import pl.edu.icm.em.socstruct.component.Household;
import pl.edu.icm.em.socstruct.component.prefab.PrefabTag;
import pl.edu.icm.em.socstruct.component.Person;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.board.urizen.household.model.AgeRange;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.bin.Histogram;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * This should be corrected using:
 * https://stat.gov.pl/files/gfx/portalinformacyjny/pl/defaultaktualnosci/5487/18/3/1/zaklady_stacjonarne_pomocy_spolecznej_w_2018.pdf
 */
public class NursingHomeUrizen {
    private final EngineIo engineIo;
    private final PopulationDensityLoader populationDensityLoader;
    private final ReplicantPrototypes prototypes;
    private final RandomDataGenerator random;
    private final int nursingHomeReplicantsCount;
    private final int nursingHomeRoomSize;
    private final int nursingHomeMaxRooms;
    private final Histogram<AgeRange> ages;
    private final Histogram<Person.Sex> sexes;
    private final AgeSexFromDistributionPicker ageSexFromDistributionPicker;
    private Entities entities;

    @WithFactory
    public NursingHomeUrizen(
            EngineIo engineIo,
            ReplicantPrototypes prototypes,
            Entities entities,
            ReplicantsPopulation replicantsPopulation,
            PopulationDensityLoader populationDensityLoader,
            AgeSexFromDistributionPicker ageSexFromDistributionPicker,
            RandomProvider randomProvider,
            @ByName("soc-struct.replicants.nursing-home.count") int nursingHomeReplicantsCount,
            @ByName("soc-struct.replicants.nursing-home.room-size") int nursingHomeRoomSize,
            @ByName("soc-struct.replicants.nursing-home.max-rooms") int nursingHomeMaxRooms) {
        this.engineIo = engineIo;
        this.prototypes = prototypes;
        this.entities = entities;
        this.populationDensityLoader = populationDensityLoader;
        this.ageSexFromDistributionPicker = ageSexFromDistributionPicker;
        this.nursingHomeReplicantsCount = nursingHomeReplicantsCount;
        this.nursingHomeRoomSize = nursingHomeRoomSize;
        this.nursingHomeMaxRooms = nursingHomeMaxRooms;
        this.engineIo.require(Household.class, Person.class, Location.class, PrefabTag.class);
        this.sexes = replicantsPopulation.getPopulation().getPeopleBySex();
        this.ages = replicantsPopulation.getPopulation().getPeopleByAge().createSubPool(
                AgeRange.AGE_70_74,
                AgeRange.AGE_75_79,
                AgeRange.AGE_80_
        );
        this.random = randomProvider.getRandomDataGenerator(NursingHomeUrizen.class);
    }

    public void fabricate() throws FileNotFoundException {
        populationDensityLoader.load();
        int maxSize = nursingHomeMaxRooms * nursingHomeRoomSize;
        int minSize = nursingHomeRoomSize;
        int count = nursingHomeReplicantsCount;
        while (count > 0) {
            int size = Math.min(random.nextInt(minSize, maxSize), count);
            generateNursingHome(size);
            count -= size;
        }
    }

    private void generateNursingHome(int nursingHomeSize) {
        engineIo.getEngine().execute(sessionFactory -> {
            Session session = sessionFactory.create();
            Entity entity = entities.createEmptyComplex(session, nursingHomeSize);
            Complex complex = entity.get(Complex.class);
            complex.setType(Complex.Type.NURSING_HOME);
            KilometerGridCell cell = populationDensityLoader.sample(random.getRandomGenerator().nextDouble());
            entity.add(cell.toLocation());
            List<Entity> complexHouseholds = complex.getHouseholds();
            int leftToCreate = nursingHomeSize;

            while (leftToCreate > 0) {
                int roomSize = Math.min(nursingHomeRoomSize, leftToCreate);
                generateRoom(roomSize, complexHouseholds, session, cell);
                leftToCreate -= roomSize;
            }
            session.close();
        });
    }

    private void generateRoom(int inhabitants, List<Entity> complexHouseholds, Session session, KilometerGridCell cell) {
            List<Entity> dependents = prototypes.nursingHomeRoom(session, complexHouseholds, cell).get(Household.class).getMembers();
            for (int i = 0; i < inhabitants; i++) {
                var sexPicked = sexes.sample(random.getRandomGenerator().nextDouble()).pick();
                var ageRangePicked = ages.sample(random.getRandomGenerator().nextDouble()).pick();
                dependents.add(prototypes.nursingHomeResident(session, sexPicked, ageSexFromDistributionPicker.getEmpiricalDistributedRandomAge(sexPicked, ageRangePicked, random.getRandomGenerator().nextDouble())));
            }
    }

}
