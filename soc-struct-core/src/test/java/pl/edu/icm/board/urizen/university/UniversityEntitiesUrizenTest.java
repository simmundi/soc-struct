package pl.edu.icm.board.urizen.university;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.model.Named;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Attendee;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.store.tablesaw.TablesawStore;
import pl.edu.icm.trurl.store.tablesaw.TablesawStoreFactory;
import tech.tablesaw.api.Row;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UniversityEntitiesUrizenTest {

    @Mock
    UniversityLoader universityLoader;
    Board board;
    UniversityEntitiesUrizen universityEntitiesUrizen;

    University bigUniversity = new University(KilometerGridCell.fromLegacyPdynCoordinates(5, 2).toLocation(), 2);
    University smallUniversity1 = new University(KilometerGridCell.fromLegacyPdynCoordinates(4, 3).toLocation(), 2);
    University smallUniversity2 = new University(KilometerGridCell.fromLegacyPdynCoordinates(9, 9).toLocation(), 1);


    @BeforeEach
    void initStructure() throws IOException {

        /* Population:

        ..........
        ....H.....
        ....2.....
        ...1H.....
        ..........
        ..........
        .........H
        ..........
        ..........
        ........H1
        h1=(4, 1); h2=(4, 3); h3=(9,6); h4=(8,9)
        h1: e = 75500 n = 874500
        h2: e = 75500 n = 872500
        h3: e = 80500 n = 869500
        h4: e = 79500 n = 866500
         */
        when(universityLoader.loadBigUniversities()).thenReturn(
                List.of(bigUniversity));
        when(universityLoader.loadSmallUniversities()).thenReturn(
                List.of(smallUniversity1, smallUniversity2));
        var engineConfig = new EngineConfiguration();
        engineConfig.setStoreFactory(new TablesawStoreFactory());
        board = new Board(engineConfig, null, null, null);
        universityEntitiesUrizen =
                new UniversityEntitiesUrizen(universityLoader,
                        board,
                        new Entities(),
                        2);
        board.require(Person.class, Named.class, Household.class, Attendee.class);
        board.load(UniversityEntitiesUrizen.class.getResourceAsStream("/universitiesTest.csv"));
    }

    @Test
    void buildEntities() throws IOException {
        universityEntitiesUrizen.buildEntities();
        TablesawStore tablesawStore = (TablesawStore) board.getEngine().getStore();
        var entitiesTable = tablesawStore.asTable("entities");
        var universityColumns = entitiesTable
                .select(entitiesTable.column("level"), entitiesTable.column("pupilCount"), entitiesTable.column("n"), entitiesTable.column("e"))
                .where(entitiesTable.column("level").isNotMissing());
        assertThat(universityColumns.rowCount()).isEqualTo(3);

        //unfortunately universityColumns.stream().toArray(); doesn't seem to work, so we're
        //taking a painful detour:
        var iter = universityColumns.stream().iterator();
        List<University> generatedUniversities = new ArrayList<>();
        while (iter.hasNext()) {
            Row row = iter.next();
            generatedUniversities.add(new University(Location.fromPl1992MeterCoords(
                    row.getInt("e"), row.getInt("n")
            ), row.getInt("pupilCount")));
        }
        assertThat(generatedUniversities).contains(bigUniversity, smallUniversity1, smallUniversity2);
    }
}
