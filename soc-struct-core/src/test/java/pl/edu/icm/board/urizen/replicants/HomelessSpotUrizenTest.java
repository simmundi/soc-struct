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
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.density.PopulationDensityLoader;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Person;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomelessSpotUrizenTest {
    @Mock
    private Board board;
    @Mock
    private ReplicantsPopulation replicantsPopulation;
    @Mock
    private Population population;
    @Spy
    private BinPool<AgeRange> ageRangeBinPool = new BinPool<>();
    @Spy
    private BinPool<Person.Sex> sexBinPool = new BinPool<>();
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

    @BeforeEach
    void before() {
        when(sessionFactory.create()).thenReturn(session);
        ageRangeBinPool.add(AgeRange.AGE_20_24, 50);
        ageRangeBinPool.add(AgeRange.AGE_25_29, 50);
        ageRangeBinPool.add(AgeRange.AGE_30_34, 50);
        ageRangeBinPool.add(AgeRange.AGE_35_39, 50);
        ageRangeBinPool.add(AgeRange.AGE_40_44, 50);
        ageRangeBinPool.add(AgeRange.AGE_45_49, 50);
        ageRangeBinPool.add(AgeRange.AGE_50_54, 50);
        ageRangeBinPool.add(AgeRange.AGE_55_59, 50);
        ageRangeBinPool.add(AgeRange.AGE_60_64, 50);
        ageRangeBinPool.add(AgeRange.AGE_65_69, 50);
        ageRangeBinPool.add(AgeRange.AGE_70_74, 50);
        ageRangeBinPool.add(AgeRange.AGE_75_79, 50);
        ageRangeBinPool.add(AgeRange.AGE_80_, 50);

        sexBinPool.add(Person.Sex.K, 500);
        sexBinPool.add(Person.Sex.M, 500);
        when(replicantsPopulation.getPopulation()).thenReturn(population);
        when(population.getPeopleByAge()).thenReturn(ageRangeBinPool);
        when(population.getPeopleBySex()).thenReturn(sexBinPool);
        when(prototypes.homelessSpotRoom(same(session), any())).thenReturn(entity);
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
        int homeless = 100;
        int maxHomelessPeopleInSpot = 10;
        int homelessSpotSize = 1;
        int maxSpots = 10;
        int percentOfMan = 83;
        int maxSpotsCount = (int) ((double) homeless / maxHomelessPeopleInSpot);
        HomelessSpotUrizen homelessSpotUrizen = new HomelessSpotUrizen(
                board, prototypes, replicantsPopulation, populationDensityLoader, ageSexFromDistributionPicker, randomProvider,
                homeless, homelessSpotSize, maxSpots, percentOfMan
        );

        // execute
        homelessSpotUrizen.fabricate();

        // assert
        verify(prototypes, times(homeless)).homelessSpotResident(same(session), any(), anyInt());
        verify(prototypes, times(homeless)).homelessSpotRoom(same(session), any());
        verify(populationDensityLoader, atMost(homeless / homelessSpotSize)).sample(anyDouble());
        verify(populationDensityLoader, atLeast(maxSpotsCount)).sample(anyDouble());
    }
}
