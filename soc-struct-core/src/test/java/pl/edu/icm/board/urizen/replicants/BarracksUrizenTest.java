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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.MockRandomProvider;
import pl.edu.icm.board.agesex.AgeSexFromDistributionPicker;
import pl.edu.icm.board.model.Complex;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.density.PopulationDensityLoader;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.board.urizen.household.model.AgeRange;
import pl.edu.icm.board.urizen.population.Population;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.bin.BinPool;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.SessionFactory;
import pl.edu.icm.trurl.ecs.EntitySystem;

import java.io.FileNotFoundException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BarracksUrizenTest {
    @Mock
    private EngineIo engineIo;
    @Mock
    private ReplicantsPopulation replicantsPopulation;
    @Mock
    private Population population;
    @Spy
    private BinPool<Person.Sex> sexBinPool = new BinPool<>();
    @Spy
    private BinPool<AgeRange> ageRangeBinPool = new BinPool<>();
    @Mock
    private PopulationDensityLoader populationDensityLoader;
    @Mock
    private Engine engine;
    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private Session session;
    @Mock
    private ReplicantPrototypes prototypes;
    @Mock
    private Entity entity;
    @Mock
    private Entity entity2;
    @Spy
    private RandomProvider randomProvider = new MockRandomProvider();
    @Mock
    private AgeSexFromDistributionPicker ageSexFromDistributionPicker;
    @Mock
    private Entities entities;

    @BeforeEach
    void before() {
        sexBinPool.add(Person.Sex.K, 600);
        sexBinPool.add(Person.Sex.M, 600);
        ageRangeBinPool.add(AgeRange.AGE_20_24, 50);
        ageRangeBinPool.add(AgeRange.AGE_25_29, 50);
        ageRangeBinPool.add(AgeRange.AGE_30_34, 50);
        ageRangeBinPool.add(AgeRange.AGE_35_39, 50);
        ageRangeBinPool.add(AgeRange.AGE_40_44, 50);
        ageRangeBinPool.add(AgeRange.AGE_45_49, 50);
        when(sessionFactory.create()).thenReturn(session);
        when(replicantsPopulation.getPopulation()).thenReturn(population);
        when(population.getPeopleByAge()).thenReturn(ageRangeBinPool);
        when(population.getPeopleBySex()).thenReturn(sexBinPool);
        when(entities
                .createEmptyComplex(same(session), anyInt()))
                .thenReturn(entity2);
        when(entity2.get(Complex.class)).thenReturn(new Complex());
        when(prototypes.barracksRoom(same(session), any(), any())).thenReturn(entity);
        when(entity.get(Household.class)).thenReturn(new Household());
        when(engineIo.getEngine()).thenReturn(engine);
        when(populationDensityLoader.sample(anyDouble())).thenReturn(KilometerGridCell.fromLegacyPdynCoordinates(200, 200));
        doAnswer(params -> {
            EntitySystem system = params.getArgument(0);
            system.execute(sessionFactory);
            return null;
        }).when(engine).execute(any());
    }

    @Test
    @DisplayName("Should create proper number of entities")
    void fabricate() throws FileNotFoundException {

        // given
        int soldiers = 100;
        int maxSoldiersInBarracks = 10;
        int roomSize = 1;
        int maxRooms = 10;
        int maxJailCount = (int) ((double) soldiers / maxSoldiersInBarracks);
        BarracksUrizen barracksUrizen = new BarracksUrizen(
                engineIo, prototypes, entities, replicantsPopulation, populationDensityLoader, ageSexFromDistributionPicker, randomProvider,
                soldiers, roomSize, maxRooms
        );

        // execute
        barracksUrizen.fabricate();

        // assert
        verify(prototypes, times(soldiers)).barracksResident(same(session), any(), anyInt());
        verify(prototypes, times(soldiers)).barracksRoom(same(session), any(), any());
        verify(populationDensityLoader, atMost(soldiers / roomSize)).sample(anyDouble());
        verify(populationDensityLoader, atLeast(maxJailCount)).sample(anyDouble());
    }
}
