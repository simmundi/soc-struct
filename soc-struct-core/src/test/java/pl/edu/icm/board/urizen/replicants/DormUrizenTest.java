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

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.EntityMocker;
import pl.edu.icm.board.MockRandomProvider;
import pl.edu.icm.board.agesex.AgeSexFromDistributionPicker;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.Complex;
import pl.edu.icm.board.geography.density.PopulationDensityLoader;
import pl.edu.icm.board.model.EducationLevel;
import pl.edu.icm.board.model.EducationalInstitution;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.board.urizen.generic.EntityStreamManipulator;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DormUrizenTest {
    private final AtomicInteger ids = new AtomicInteger(10000);
    @Mock
    private Board board;
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
    @Spy
    private EntityStreamManipulator entityStreamManipulator;
    @Mock
    private AgeSexFromDistributionPicker ageSexFromDistributionPicker;
    @Mock
    private RandomGenerator randomGenerator;
    @Mock
    private Entities entities;

    private EntityMocker entityMocker;

    @BeforeEach
    void before() {
        entityMocker = new EntityMocker(session, EducationalInstitution.class, Location.class);
        sexBinPool.add(Person.Sex.K, 500);
        sexBinPool.add(Person.Sex.M, 500);
        ageRangeBinPool.add(AgeRange.AGE_20_24, 1000);

        when(sessionFactory.create()).thenReturn(session);
        when(replicantsPopulation.getPopulation()).thenReturn(population);
        when(population.getPeople20to24()).thenReturn(ageRangeBinPool);
        when(population.getPeopleBySex()).thenReturn(sexBinPool);
        when(entities
                .createEmptyComplex(same(session), anyInt()))
                .thenReturn(entity2);
        when(entity2.get(Complex.class)).thenReturn(new Complex());
        when(prototypes.dormRoom(same(session), any(), any())).thenReturn(entity);
        when(prototypes.dormResident(any(), any(), anyInt())).thenReturn(
                entityMocker.entity(ids.incrementAndGet())
        );
        when(entity.get(Household.class)).thenReturn(new Household());
        when(board.getEngine()).thenReturn(engine);
        when(populationDensityLoader.isPopulated(any())).thenReturn(true);
        doAnswer(params -> {
            EntitySystem system = params.getArgument(0);
            system.execute(sessionFactory);
            return null;
        }).when(engine).execute(any());
        when(engine.streamDetached()).thenReturn(Stream.of(
                entityMocker.entity(0, new EducationalInstitution(EducationLevel.U, 300, 0), (KilometerGridCell.fromLegacyPdynCoordinates(300, 400)).toLocation()),
                entityMocker.entity(1, new EducationalInstitution(EducationLevel.U, 400, 0), (KilometerGridCell.fromLegacyPdynCoordinates(304, 400)).toLocation()),
                entityMocker.entity(2, new EducationalInstitution(EducationLevel.BU, 1000, 0), (KilometerGridCell.fromLegacyPdynCoordinates(400, 400)).toLocation()),
                entityMocker.entity(3, new EducationalInstitution(EducationLevel.BU, 2000, 0), (KilometerGridCell.fromLegacyPdynCoordinates(320, 400)).toLocation()),
                entityMocker.entity(4, new EducationalInstitution(EducationLevel.BU, 3000, 0), (KilometerGridCell.fromLegacyPdynCoordinates(360, 400)).toLocation())
        ));
        when(randomProvider.getRandomGenerator(any(Class.class))).thenReturn(randomGenerator);
        when(randomGenerator.nextDouble()).thenReturn(0.5);
        doAnswer((Answer<Integer>) invocation -> {
            int value = invocation.getArgument(0);
            return value - 1;
        }).when(randomGenerator).nextInt(anyInt());

    }

    @Test
    @DisplayName("Should create dorms and students")
    public void fabricate() throws FileNotFoundException {
        // given
        DormUrizen dormUrizen = new DormUrizen(
                board,
                prototypes,
                entities, entityStreamManipulator,
                populationDensityLoader,
                ageSexFromDistributionPicker,
                randomProvider,
                replicantsPopulation,
                1234, 2, 100, 10);

        // execute
        dormUrizen.fabricate();

        // assert
        verify(prototypes, times(1234)).dormResident(any(), any(), anyInt());
        verify(prototypes, times(1234 / 2)).dormRoom(any(), any(), any());
    }

}
