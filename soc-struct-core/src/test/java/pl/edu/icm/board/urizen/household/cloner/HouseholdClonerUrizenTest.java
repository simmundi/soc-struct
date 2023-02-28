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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.EntityMocker;
import pl.edu.icm.board.MockRandomProvider;
import pl.edu.icm.board.agesex.AgeSexFromDistributionPicker;
import pl.edu.icm.board.model.AdministrationUnit;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.population.trusted.CountyPopulationLoader;
import pl.edu.icm.board.urizen.replicants.ReplicantsCounter;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.bin.BinPool;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.SessionFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HouseholdClonerUrizenTest {
    public static final int ANY_ID = 1;
    @Mock
    Engine engine;
    @Mock
    SessionFactory sessionFactory;
    @Mock
    Session session;
    @Mock
    EngineIo engineIo;
    @Mock
    CountyPopulationLoader countyPopulationLoader;
    @Spy
    RandomProvider randomProvider = new MockRandomProvider();
    @Mock
    FamilyShapeStatsService familyShapeStatsService;
    @Mock
    ReplicantsCounter replicantsCounter;
    @InjectMocks
    HouseholdClonerUrizen householdClonerUrizen;
    @Spy
    private FamilyShapeStats familyShapeStats;
    @Mock
    private AgeSexFromDistributionPicker ageSexFromDistributionPicker;

    private EntityMocker mock;

    @BeforeEach
    void configure() {
        mock = new EntityMocker(session, Household.class, Person.class, AdministrationUnit.class);

        when(engineIo.getEngine()).thenReturn(engine);
        doAnswer(call -> {
            ((EntitySystem) call.getArgument(0)).execute(sessionFactory);
            return null;
        }).when(engine).execute(any());

        familyShapeStats = new FamilyShapeStats();
        when(familyShapeStatsService.countStats()).thenReturn(familyShapeStats);
    }

    @Test
    @DisplayName("Should correctly scale up population by cloning households")
    @Disabled("TODO: fix testing apparatus, broken after refactors")
    void cloneHouseholds__lots_of_replicants() {

        // given
        setupEntities();
        when(countyPopulationLoader.totalPopulation()).thenReturn(51);
        when(replicantsCounter.getReplicantsCount()).thenReturn(29);

        // execute
        householdClonerUrizen.cloneHouseholds();

        // assert
//        assertThat(
//                session.()
//                        .filter(e -> e.get(Household.class) != null)
//                        .map(e -> e.get(AdministrationUnit.class).getTeryt())
//                        .collect(Collectors.toList()))
//                .containsExactly("kraków");
    }

    @Test
    @DisplayName("Should correctly scale up population by cloning households")
    @Disabled("TODO: fix testing apparatus, broken after refactors")
    void cloneHouseholds() {
        // given
        setupEntities();
        when(countyPopulationLoader.totalPopulation()).thenReturn(51);

        // execute
        householdClonerUrizen.cloneHouseholds();

        // assert
        List<Entity> households = null;
//        session.entityStream()
//                .filter(e -> e.get(Household.class) != null)
//                .collect(Collectors.toList());

        assertThat(households).extracting(
                        e -> e.get(AdministrationUnit.class).getTeryt(),
                        e -> e.get(Household.class).getMembers().size())
                .containsExactly(
                        tuple("warszawa", 2),
                        tuple("warszawa", 1),
                        tuple("warszawa", 1),
                        tuple("warszawa", 1),
                        tuple("warszawa", 1),
                        tuple("warszawa", 1),
                        tuple("warszawa", 1),
                        tuple("warszawa", 1),
                        tuple("warszawa", 1),
                        tuple("kraków", 10),
                        tuple("kraków", 10),
                        tuple("kraków", 10)
                );
    }

    private void setupEntities() {
        // ten people already in Warsaw:
        //  - 8 households of 1 elderny man,
        //  - 1 household of a young couple
        // target Warsaw population of 20
        familyShapeStats.populationByTeryt.put("warszawa", new AtomicInteger(10));
        BinPool<HouseholdShape> warszawa = new BinPool<>();
        familyShapeStats.shapesByTeryt.put("warszawa", warszawa);
        when(countyPopulationLoader.populationOf("warszawa")).thenReturn(20);
//        warszawa.add(HouseholdShape.tryCreate(
//                entity(ANY_ID,
//                        household(
//                                entity(ANY_ID, person(27, Person.Sex.M)),
//                                entity(ANY_ID, person(27, Person.Sex.K))),
//                        au("warszawa")), ageSexFromDistributionPicker), 1);
//        warszawa.add(HouseholdShape.tryCreate(
//                entity(ANY_ID,
//                        household(
//                                entity(ANY_ID, person(82, Person.Sex.M))),
//                        au("warszawa")), ageSexFromDistributionPicker), 8);

        // ten people in Cracow:
        // - 1 household with 10 middle-aged people in it
        // target Cracow population: 31
        BinPool<HouseholdShape> krakow = new BinPool<>();
        familyShapeStats.shapesByTeryt.put("kraków", krakow);
        familyShapeStats.populationByTeryt.put("kraków", new AtomicInteger(10));
        when(countyPopulationLoader.populationOf("kraków")).thenReturn(31);
//        krakow.add(HouseholdShape.tryCreate(
//                entity(ANY_ID,
//                        household(
//                                entity(ANY_ID, person(50, Person.Sex.M)),
//                                entity(ANY_ID, person(50, Person.Sex.M)),
//                                entity(ANY_ID, person(50, Person.Sex.M)),
//                                entity(ANY_ID, person(50, Person.Sex.M)),
//                                entity(ANY_ID, person(50, Person.Sex.M)),
//                                entity(ANY_ID, person(50, Person.Sex.M)),
//                                entity(ANY_ID, person(50, Person.Sex.M)),
//                                entity(ANY_ID, person(50, Person.Sex.M)),
//                                entity(ANY_ID, person(50, Person.Sex.M)),
//                                entity(ANY_ID, person(50, Person.Sex.M))),
//                        au("kraków")), ageSexFromDistributionPicker), 1);
    }
}
