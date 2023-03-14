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
import pl.edu.icm.board.EntityMocker;
import pl.edu.icm.board.MockRandomProvider;
import pl.edu.icm.board.agesex.AgeSexFromDistributionPicker;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.commune.Commune;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.board.geography.commune.TerytsOfBigCities;
import pl.edu.icm.board.geography.density.PopulationDensityLoader;
import pl.edu.icm.board.model.AdministrationUnit;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Replicant;
import pl.edu.icm.board.model.ReplicantType;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.model.Workplace;
import pl.edu.icm.board.urizen.generic.EntityStreamManipulator;
import pl.edu.icm.board.urizen.household.model.AgeRange;
import pl.edu.icm.board.urizen.population.Population;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.board.workplace.ProfessionalActivityAssessor;
import pl.edu.icm.trurl.bin.BinPool;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.SessionFactory;
import pl.edu.icm.trurl.ecs.EntitySystem;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImmigrantsSpotUrizenTest {
    public static final String TERYT_WARSAW = "1465011";
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
    @Spy
    private RandomProvider randomProvider = new MockRandomProvider();
    @Mock
    private CommuneManager communeManager;
    @Spy
    private List<KilometerGridCell> cells = new ArrayList<>();
    @Mock
    AgeSexFromDistributionPicker ageSexFromDistributionPicker;
    @Mock
    private TerytsOfBigCities terytsOfBigCities;

    private EntityMocker entityMocker;

    @BeforeEach
    void before() {
        entityMocker = new EntityMocker(session, Workplace.class, Location.class, AdministrationUnit.class, Person.class, Replicant.class);
        sexBinPool.add(Person.Sex.K, 850);
        sexBinPool.add(Person.Sex.M, 850);
        ageRangeBinPool.add(AgeRange.AGE_0_4, 100);
        ageRangeBinPool.add(AgeRange.AGE_5_9, 100);
        ageRangeBinPool.add(AgeRange.AGE_10_14, 100);
        ageRangeBinPool.add(AgeRange.AGE_15_19, 100);
        ageRangeBinPool.add(AgeRange.AGE_20_24, 100);
        ageRangeBinPool.add(AgeRange.AGE_25_29, 100);
        ageRangeBinPool.add(AgeRange.AGE_30_34, 100);
        ageRangeBinPool.add(AgeRange.AGE_35_39, 100);
        ageRangeBinPool.add(AgeRange.AGE_40_44, 100);
        ageRangeBinPool.add(AgeRange.AGE_45_49, 100);
        ageRangeBinPool.add(AgeRange.AGE_50_54, 100);
        ageRangeBinPool.add(AgeRange.AGE_55_59, 100);
        ageRangeBinPool.add(AgeRange.AGE_60_64, 100);
        ageRangeBinPool.add(AgeRange.AGE_65_69, 100);
        ageRangeBinPool.add(AgeRange.AGE_70_74, 100);
        ageRangeBinPool.add(AgeRange.AGE_75_79, 100);
        ageRangeBinPool.add(AgeRange.AGE_80_, 100);
        when(sessionFactory.create()).thenReturn(session);
        when(replicantsPopulation.getPopulation()).thenReturn(population);
        when(population.getPeopleByAge()).thenReturn(ageRangeBinPool);
        when(population.getPeopleBySex()).thenReturn(sexBinPool);
        when(prototypes.immigrantsSpotRoom(same(session), any())).thenReturn(entity);
        when(entity.get(Household.class)).thenReturn(new Household());
        when(engineIo.getEngine()).thenReturn(engine);
        doAnswer(params -> {
            EntitySystem system = params.getArgument(0);
            system.execute(sessionFactory);
            return null;
        }).when(engine).execute(any());
        when(engine.streamDetached()).thenReturn(Stream.of(
                workplace(10, 647, 487, TERYT_WARSAW),
                workplace(30, 647, 487, TERYT_WARSAW),
                workplace(600, 647, 487, TERYT_WARSAW),
                workplace(600, 647, 487, "asdasdasdasd")));
        when(terytsOfBigCities.getAllTeryts()).thenReturn(List.of(TERYT_WARSAW));
        cells.add(KilometerGridCell.fromPl1992ENMeters(647, 487));
        when(communeManager.communeForTeryt(anyString())).thenReturn(Optional.of(new Commune(TERYT_WARSAW, "Warszawa", Set.of(KilometerGridCell.fromPl1992ENMeters(647, 487)))));
        when(communeManager.communeAt(any(KilometerGridCell.class))).thenReturn(new Commune(TERYT_WARSAW, "Warszawa", Set.of(KilometerGridCell.fromPl1992ENMeters(647, 487))));
        when(prototypes.immigrantsSpotResident(any(), any(), anyInt())).thenReturn(immigrant());
    }

    int id = 0;

    private Entity workplace(int i, int e, int n, String teryt) {
        return entityMocker.entity(id++,
                new Workplace((short) i),
                KilometerGridCell.fromPl1992ENMeters(e, n).toLocation(),
                new AdministrationUnit(teryt));
    }

    private Entity immigrant() {
        Person person = new Person();
        person.setSex(Person.Sex.M);
        person.setAge(30);
        Replicant replicant = new Replicant();
        replicant.setType(ReplicantType.IMMIGRANTS_SPOT);
        return entityMocker.entity(id++, person, replicant);
    }

    @Test
    @DisplayName("Should create proper number of entities")
    void fabricate() throws FileNotFoundException {
        // given
        int immigrants = 91;
        int roomSize = 15;
        int maxRooms = 1;

        int ImmigrantsRoomCount = (int) Math.ceil((double) immigrants / roomSize);
        ImmigrantsSpotUrizen immigrantsSpotUrizen = new ImmigrantsSpotUrizen(
                engineIo,
                communeManager,
                new EntityStreamManipulator(),
                prototypes,
                replicantsPopulation,
                populationDensityLoader,
                randomProvider,
                new ProfessionalActivityAssessor(),
                ageSexFromDistributionPicker,
                terytsOfBigCities,
                immigrants, roomSize, maxRooms
        );

        // execute
        immigrantsSpotUrizen.fabricate();

        // assert
        verify(prototypes, times(immigrants)).immigrantsSpotResident(same(session), any(), anyInt());
        verify(prototypes, atLeast(ImmigrantsRoomCount)).immigrantsSpotRoom(same(session), any());
        verify(prototypes, atMost(immigrants)).immigrantsSpotRoom(same(session), any());
    }
}
