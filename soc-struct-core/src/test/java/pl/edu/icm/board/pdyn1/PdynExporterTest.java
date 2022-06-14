package pl.edu.icm.board.pdyn1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.commune.Commune;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.util.StaticSelectors;
import pl.edu.icm.trurl.store.tablesaw.TablesawStoreFactory;
import pl.edu.icm.trurl.util.TextFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdynExporterTest {
    @Mock
    DatFileCreator datFileCreator;
    @Mock
    CommuneManager communeManager;
    @Mock
    StaticSelectors staticSelectors;

    ByteArrayOutputStream gdStream = new ByteArrayOutputStream();
    TextFile gd;

    ByteArrayOutputStream agenciStream = new ByteArrayOutputStream();
    TextFile agenci;

    ByteArrayOutputStream zakladyStream = new ByteArrayOutputStream();
    TextFile zaklady;


    ByteArrayOutputStream szkolyStream = new ByteArrayOutputStream();
    TextFile szkoly;

    ByteArrayOutputStream liceaStream = new ByteArrayOutputStream();
    TextFile licea;


    ByteArrayOutputStream bigUniversitiesStream = new ByteArrayOutputStream();
    TextFile bigUniversities;

    ByteArrayOutputStream smallUniversitiesStream = new ByteArrayOutputStream();
    TextFile smallUniversities;

    PdynExporter pdynExporter;

    @BeforeEach
    public void before() throws IOException {
        Board board = new Board(new EngineConfiguration(new TablesawStoreFactory()), null);

        gd = new TextFile(new PrintWriter(gdStream));
        agenci = new TextFile(new PrintWriter(agenciStream));
        zaklady = new TextFile(new PrintWriter(zakladyStream));
        szkoly = new TextFile(new PrintWriter(szkolyStream));
        licea = new TextFile(new PrintWriter(liceaStream));
        bigUniversities = new TextFile(new PrintWriter(bigUniversitiesStream));
        smallUniversities = new TextFile(new PrintWriter(smallUniversitiesStream));

        pdynExporter = new PdynExporter(datFileCreator, board, communeManager, true);
        board.load(PdynExporter.class.getResourceAsStream("/pdyn15.csv"));

        when(communeManager.getCommunes()).thenReturn(List.of(
                new Commune("1412132", "a", Set.of(KilometerGridCell.fromLegacyPdynCoordinates(111,222))),
                new Commune("1061011", "b", Set.of(KilometerGridCell.fromLegacyPdynCoordinates(333,444))),
                new Commune("0810021", "c", Set.of(KilometerGridCell.fromLegacyPdynCoordinates(555,555)))
        ));
    }

    @Test
    @DisplayName("Should export data")
    public void execute() throws IOException {
        // given

        when(datFileCreator.create(Paths.get("dir", "gd.dat").toString())).thenReturn(gd);
        when(datFileCreator.create(Paths.get("dir", "agenci.dat").toString())).thenReturn(agenci);
        when(datFileCreator.create(Paths.get("dir", "zaklady.dat").toString())).thenReturn(zaklady);
        when(datFileCreator.create(Paths.get("dir","szkoly.dat").toString())).thenReturn(szkoly);
        when(datFileCreator.create(Paths.get("dir", "licea.dat").toString())).thenReturn(licea);
        when(datFileCreator.create(Paths.get("dir", "jew.dat").toString())).thenReturn(smallUniversities);
        when(datFileCreator.create(Paths.get("dir", "djew.dat").toString())).thenReturn(bigUniversities);

        // execute
        pdynExporter.export("dir");

        // assert
        assertThat(gdStream.toString()).isEqualTo(("IloscGD 7\n" +
                "2 0 1\n" +
                "630 691\n" +
                "3 2 3 4\n" +
                "544 200\n" +
                "1 5\n" +
                "544 205\n" +
                "1 6\n" +
                "539 208\n" +
                "2 7 8\n" +
                "545 205\n" +
                "2 9 10\n" +
                "543 201\n" +
                "4 11 12 13 14\n" +
                "630 691\n").replaceAll("\\n", System.lineSeparator()));
        assertThat(agenciStream.toString()).isEqualTo(("IloscAgentow 15\n" +
                "73 0\n" +
                "28 1\n" +
                "56 0\n" +
                "36 1\n" +
                "39 1\n" +
                "54 0\n" +
                "44 1\n" +
                "61 0\n" +
                "1 0\n" +
                "41 1\n" +
                "22 1\n" +
                "5 0\n" +
                "10 1\n" +
                "14 1\n" +
                "17 0\n").replaceAll("\\n", System.lineSeparator()));
        assertThat(zakladyStream.toString()).isEqualTo(("3\n" +
                "2\t1\t2\n" +
                "222  111  0\n" +
                "3\t3\t5\t6\n" +
                "444  333  0\n" +
                "1\t4\n" +
                "555  555  0\n").replaceAll("\\n", System.lineSeparator()));

        assertThat(szkolyStream.toString()).isEqualTo(("LiczbaPrzedszkoli 1\n" +
                "LiczbaPodstawowek 1\n" +
                "LiczbaGimnazjow 0\n" +
                "1\t11\n" +
                "630 691\n" +
                "2\t12\t13\n" +
                "630 691\n").replaceAll("\\n", System.lineSeparator()));
        assertThat(liceaStream.toString()).isEqualTo(("1\n" +
                "1\t14\n" +
                "630 691\n").replaceAll("\\n", System.lineSeparator()));

        assertThat(bigUniversitiesStream.toString()).isEqualTo(("1\n" +
                "1\t10\n" +
                "100 100 university\n").replaceAll("\\n", System.lineSeparator()));

        assertThat(smallUniversitiesStream.toString()).isEqualTo(("0\n" +
                "").replaceAll("\\n", System.lineSeparator()));
    }

}
