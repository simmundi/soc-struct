package pl.edu.icm.board.urizen.place;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.MockRandomProvider;
import pl.edu.icm.board.geography.density.PopulationDensityLoader;
import pl.edu.icm.board.model.*;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.board.urizen.generic.EntityStreamManipulator;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.util.StaticSelectors;
import pl.edu.icm.trurl.store.tablesaw.TablesawStore;
import pl.edu.icm.trurl.store.tablesaw.TablesawStoreFactory;
import tech.tablesaw.api.Table;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignPatientsToHealthcareUrizenTest {
    private AssignPatientsToHealthcareUrizen assigner;
    Board board;
    @Spy
    private EntityStreamManipulator entityStreamManipulator = new EntityStreamManipulator();
    @Spy
    private Entities entities = new Entities();
    @Spy
    private RandomProvider randomProvider = new MockRandomProvider();
    @Mock
    private PopulationDensityLoader populationDensityLoader;

    @BeforeEach
    void before() throws IOException {
        EngineConfiguration engineConfiguration = new EngineConfiguration();
        engineConfiguration.setStoreFactory(new TablesawStoreFactory());
        board = Mockito.spy(new Board(engineConfiguration, null, null, null));
        StaticSelectors staticSelectors = new StaticSelectors(engineConfiguration);
        assigner = new AssignPatientsToHealthcareUrizen(board, entities, entityStreamManipulator, randomProvider, populationDensityLoader, staticSelectors);
        board.require(Healthcare.class, Location.class, Named.class, Household.class, Person.class, Patient.class);
        board.load(AssignAttendeesToInstitutionsUrizen.class.getResourceAsStream("/healthcareAssignerTest.csv"));
        when(populationDensityLoader.isPopulated(any())).thenReturn(true);
    }

    @Test
    @DisplayName("Should assign people to healthcare units")
    void test () {
        assigner.assignToHealthcare();
        var entities = ((TablesawStore)board.getEngine().getStore()).asTable("entities");

        assertThat(whereAssigned(entities, "9").rowCount()).isEqualTo(2);
        assertThat(whereAssigned(entities, "a").rowCount()).isEqualTo(3);
        assertThat(whereAssigned(entities, "b").rowCount()).isEqualTo(1);
    }

    private Table whereAssigned(Table entities, String id) {
        return entities.where(
                entities.textColumn("healthcare").isEqualTo(id));
    }
}
