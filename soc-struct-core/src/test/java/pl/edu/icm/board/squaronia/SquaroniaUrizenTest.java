package pl.edu.icm.board.squaronia;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.store.tablesaw.TablesawStore;
import pl.edu.icm.trurl.store.tablesaw.TablesawStoreFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class SquaroniaUrizenTest {

    @Test
    @DisplayName("should generate specific age structure")
    void withAgeGroupShare(){
        //given
        EngineConfiguration engineConfiguration = new EngineConfiguration();
        engineConfiguration.setStoreFactory(new TablesawStoreFactory());
        Board board = new Board(engineConfiguration, null, null, null);
        RandomProvider randomProvider = new RandomProvider(0);
        var squaroniaUrizen = new SquaroniaUrizen(board,3,100,10,50, randomProvider);

        //execute
        squaroniaUrizen.withAgeGroupShare(AgeRange.AGE_0_4,1).withAgeGroupShare(AgeRange.AGE_15_19,1).build();

        //given
        var entities = ((TablesawStore)board.getEngine().getComponentStore()).asTable("entities");

        //assert
        assertThat(entities.where(
                entities.column("age").isNotMissing().andNot(
                        entities.intColumn("age").isBetweenInclusive(0,4).or(
                                entities.intColumn("age").isBetweenInclusive(15,19)
                        )
                )
        ).rowCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("shoud build squaronia")
    void build(){
        //given
        EngineConfiguration engineConfiguration = new EngineConfiguration();
        engineConfiguration.setStoreFactory(new TablesawStoreFactory());
        Board board = new Board(engineConfiguration, null, null, null);
        RandomProvider randomProvider = new RandomProvider(0);
        var squaroniaUrizen = new SquaroniaUrizen(board,4,100,10,50, randomProvider);
        //execute
        squaroniaUrizen.withAgeGroupShare(AgeRange.AGE_0_4,1).build();
        //given
        var entities = ((TablesawStore)board.getEngine().getComponentStore()).asTable("entities");

        //assert
        assertThat(entities.where(
                entities.column("age").isNotMissing()
        ).rowCount()).isEqualTo(100);

        assertThat(entities.where(
                entities.intColumn("age").isNotMissing().andNot(
                        entities.intColumn("age").isBetweenInclusive(0,4)
                )).rowCount()).isEqualTo(0);

        assertThat(
                entities.where(
                entities.intColumn("age").isNotMissing()
        ).xTabPercents("sex").row(0).getDouble(1)
        ).isCloseTo(0.5, Percentage.withPercentage(3));

        assertThat(entities.where(
                entities.intColumn("n").isNotMissing().andNot(
                        entities.intColumn("n").isBetweenInclusive(0,9).or(
                                entities.intColumn("e").isBetweenInclusive(0,9)
                        )
                )
        ).rowCount()
        ).isEqualTo(0);
    }

}
