package pl.edu.icm.board.urizen.replicants;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.Board;
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
class MonasteryUrizenTest {
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
    @Spy
    private RandomProvider randomProvider = new MockRandomProvider();
    @Mock
    private AgeSexFromDistributionPicker ageSexFromDistributionPicker;
    @Mock
    private Entities entities;
    @Mock
    private Entity entity2;

    @BeforeEach
    void before() {
        sexBinPool.add(Person.Sex.K, 6000);
        sexBinPool.add(Person.Sex.M, 7000);
        ageRangeBinPool.add(AgeRange.AGE_20_24, 1000);
        ageRangeBinPool.add(AgeRange.AGE_25_29, 1000);
        ageRangeBinPool.add(AgeRange.AGE_30_34, 1000);
        ageRangeBinPool.add(AgeRange.AGE_35_39, 1000);
        ageRangeBinPool.add(AgeRange.AGE_40_44, 1000);
        ageRangeBinPool.add(AgeRange.AGE_45_49, 1000);
        ageRangeBinPool.add(AgeRange.AGE_50_54, 1000);
        ageRangeBinPool.add(AgeRange.AGE_55_59, 1000);
        ageRangeBinPool.add(AgeRange.AGE_60_64, 1000);
        ageRangeBinPool.add(AgeRange.AGE_65_69, 1000);
        ageRangeBinPool.add(AgeRange.AGE_70_74, 1000);
        ageRangeBinPool.add(AgeRange.AGE_75_79, 1000);
        ageRangeBinPool.add(AgeRange.AGE_80_, 1000);

        when(sessionFactory.create()).thenReturn(session);
        when(replicantsPopulation.getPopulation()).thenReturn(population);
        when(population.getPeopleByAge()).thenReturn(ageRangeBinPool);
        when(population.getPeopleBySex()).thenReturn(sexBinPool);
        when(entities
                .createEmptyComplex(same(session), anyInt()))
                .thenReturn(entity2);
        when(entity2.get(Complex.class)).thenReturn(new Complex());
        when(prototypes.monasteryRoom(same(session), any(), any())).thenReturn(entity);
        when(entity.get(Household.class)).thenReturn(new Household());
        when(board.getEngine()).thenReturn(engine);
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
        int monasteries = 100;
        int maxRoomsInOneHouse = 5;
        int roomSize = 2;
        int maxReligiousHouses = (int) ((double) monasteries / (roomSize * maxRoomsInOneHouse));

        MonasteryUrizen monasteryUrizen = new MonasteryUrizen(
                board, prototypes, entities, replicantsPopulation, populationDensityLoader, ageSexFromDistributionPicker, randomProvider,
                monasteries, roomSize, maxRoomsInOneHouse
        );

        // execute
        monasteryUrizen.fabricate();

        // assert
        verify(prototypes, times(monasteries)).monasteryResident(same(session), any(), anyInt());
        verify(prototypes, atMost(monasteries)).monasteryRoom(same(session), any(), any());
        verify(prototypes, atLeast(maxReligiousHouses)).monasteryRoom(same(session), any(), any());
        verify(populationDensityLoader, atMost(monasteries / roomSize)).sample(anyDouble());
        verify(populationDensityLoader, atLeast(maxReligiousHouses)).sample(anyDouble());
    }
}
