package pl.edu.icm.board.urizen.household;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.MockRandomProvider;
import pl.edu.icm.board.geography.prg.AddressPointManager;
import pl.edu.icm.board.geography.prg.model.AddressPoint;
import pl.edu.icm.board.model.Complex;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.store.tablesaw.TablesawStoreFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HouseholdsFromGridToAddressPointsUrizenTest {
    private Board board;
    @Mock
    private AddressPointManager addressPointsManager;
    @Spy
    private RandomProvider randomProvider = new MockRandomProvider();
    private Entities entities = new Entities();

    @BeforeEach
    void setup() throws IOException {
        board = new Board(new EngineConfiguration(new TablesawStoreFactory()), null);
        board.load(HouseholdsFromGridToAddressPointsUrizenTest.class.getResourceAsStream("/fromgridtoaddresspoint.csv"),
                Person.class, Household.class, Complex.class, Location.class);
        var ap1 = new AddressPoint();
        ap1.setEasting(638846);
        ap1.setNorthing(498184);
        var ap2 = new AddressPoint();
        ap2.setEasting(438273);
        ap2.setNorthing(604921);

        when(addressPointsManager.streamAddressPoints()).thenReturn(Stream.of(ap1, ap2));

    }

    @Test
    void assignHouseholds() {
        // given
        var urizen = new HouseholdsFromGridToAddressPointsUrizen(addressPointsManager,
                board, randomProvider, entities);
        // execute
        urizen.assignHouseholds();
        var householdsInComplexes = board.getEngine().streamDetached()
                .filter(e -> e.get(Complex.class) != null)
                .map(e -> e.get(Complex.class)
                        .getHouseholds().stream()
                        .map(Entity::getId)
                        .collect(Collectors.toList())).collect(Collectors.toList());

        var householdsInBoard = board.getEngine().streamDetached()
                .filter(e -> e.get(Household.class) != null).map(Entity::getId).collect(Collectors.toList());
        // assert
        assertThat(householdsInComplexes.get(0)).isEqualTo(List.of(householdsInBoard.get(2)));
        assertThat(householdsInComplexes.get(1)).isEqualTo(List.of(householdsInBoard.get(0),householdsInBoard.get(1)));
    }
}
