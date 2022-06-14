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
class NursingHomeUrizenTest {
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
    private Entities entities;
    @Mock
    private Entity entity1;
    @Mock
    private Entity entity2;
    @Spy
    private RandomProvider randomProvider = new MockRandomProvider();
    @Mock
    private AgeSexFromDistributionPicker ageSexFromDistributionPicker;

    @BeforeEach
    void before() {
        sexBinPool.add(Person.Sex.K, 500);
        sexBinPool.add(Person.Sex.M, 500);
        ageRangeBinPool.add(AgeRange.AGE_80_, 1000);

        when(sessionFactory.create()).thenReturn(session);
        when(replicantsPopulation.getPopulation()).thenReturn(population);
        when(population.getPeopleByAge()).thenReturn(ageRangeBinPool);
        when(population.getPeopleBySex()).thenReturn(sexBinPool);
        when(entities
                .createEmptyComplex(same(session), anyInt()))
                .thenReturn(entity2);
        when(entity2.get(Complex.class)).thenReturn(new Complex());
        when(prototypes
                .nursingHomeRoom(same(session), any(), any()))
                .thenReturn(entity1);
        when(entity1.get(Household.class)).thenReturn(new Household());
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
        int seniors = 100;
        int maxSeniorsInOneDsp = 5;
        int roomSize = 1;
        int maxDsps = (int) ((double) seniors / maxSeniorsInOneDsp);

        NursingHomeUrizen dspUrizen = new NursingHomeUrizen(
                board, prototypes, entities, replicantsPopulation, populationDensityLoader,
                ageSexFromDistributionPicker, this.randomProvider, seniors, roomSize, maxSeniorsInOneDsp
        );

        // execute
        dspUrizen.fabricate();

        // assert
        verify(prototypes, times(seniors)).nursingHomeResident(same(session), any(), anyInt());
        verify(prototypes, times(seniors)).nursingHomeRoom(same(session), any(), any());
        verify(populationDensityLoader, atMost(seniors / roomSize)).sample(anyDouble());
        verify(populationDensityLoader, atLeast(maxDsps)).sample(anyDouble());
    }
}
