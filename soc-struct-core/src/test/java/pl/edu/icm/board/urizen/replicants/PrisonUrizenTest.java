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
import pl.edu.icm.board.geography.prg.model.AddressLookupResult;
import pl.edu.icm.board.geography.prg.model.GeocodedPoi;
import pl.edu.icm.board.model.Complex;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.board.urizen.household.model.AgeRange;
import pl.edu.icm.board.urizen.population.Population;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.bin.BinPool;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.SessionFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrisonUrizenTest {
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
    @Spy
    private AddressLookupResult addressLookupResult = new AddressLookupResult();
    @Spy
    private PrisonFromCsv prisonFromCsv = new PrisonFromCsv();
    @Mock
    private PrisonGeodecoder prisonGeodecoder;
    private int prisoners = 100;
    @BeforeEach
    void before() throws IOException {
        sexBinPool.add(Person.Sex.K, 500);
        sexBinPool.add(Person.Sex.M, 500);
        ageRangeBinPool.add(AgeRange.AGE_20_24, 1000);

        when(sessionFactory.create()).thenReturn(session);
        when(replicantsPopulation.getPopulation()).thenReturn(population);
        when(population.getPeople20to100()).thenReturn(ageRangeBinPool);
        when(population.getPeopleBySex()).thenReturn(sexBinPool);
        when(entities
                .createEmptyComplex(same(session), anyInt()))
                .thenReturn(entity2);
        when(entity2.get(Complex.class)).thenReturn(new Complex());
        when(prototypes.prisonRoom(same(session), any(), any())).thenReturn(entity);
        when(entity.get(Household.class)).thenReturn(new Household());
        when(engineIo.getEngine()).thenReturn(engine);
        doAnswer(params -> {
            EntitySystem system = params.getArgument(0);
            system.execute(sessionFactory);
            return null;
        }).when(engine).execute(any());
        addressLookupResult.setLocation(Location.fromPl1992MeterCoords(1,1));
        prisonFromCsv.setType(PrisonFromCsv.Type.PRISON_K);
        prisonFromCsv.setName("Areszt śledczy nr 1 w Warszawie");
        prisonFromCsv.setPrisonCount(prisoners);
        doAnswer(params -> {
            Consumer<GeocodedPoi<PrisonFromCsv>> consumer = params.getArgument(0);
            consumer.accept(new GeocodedPoi<>(addressLookupResult, prisonFromCsv));
            return null;
        }).when(prisonGeodecoder).foreach(any());

    }

    @Test
    @DisplayName("Should create proper number of entities")
    void fabricate() throws FileNotFoundException {
        // given
        int roomSize = 2;

        PrisonUrizen prisonUrizen = new PrisonUrizen(
                engineIo, prototypes, entities, replicantsPopulation, ageSexFromDistributionPicker, randomProvider,
                prisonGeodecoder, roomSize
        );

        // execute
        prisonUrizen.fabricate();

        // assert
        verify(prototypes, times(prisoners)).prisonResident(same(session), any(), anyInt());
        verify(prototypes, atLeast(prisoners / roomSize)).prisonRoom(same(session), any(), any());
        verify(prototypes, atMost(prisoners)).prisonRoom(same(session), any(), any());
    }
}
