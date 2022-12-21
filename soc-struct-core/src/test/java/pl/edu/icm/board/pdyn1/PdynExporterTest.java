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

package pl.edu.icm.board.pdyn1;

import net.snowyhollows.bento.config.WorkDir;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.MockRandomProvider;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.commune.Commune;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.em.common.DebugTextFile;
import pl.edu.icm.em.common.DebugTextFileService;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.store.tablesaw.TablesawStoreFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdynExporterTest {
    @Mock
    DebugTextFileService datFileCreator;
    @Mock
    WorkDir workDir;
    @Mock
    CommuneManager communeManager;
    @Mock
    PdynIdExporter idExporter;

    ByteArrayOutputStream gdStream = new ByteArrayOutputStream();
    DebugTextFile gd;

    ByteArrayOutputStream agenciStream = new ByteArrayOutputStream();
    DebugTextFile agenci;

    ByteArrayOutputStream zakladyStream = new ByteArrayOutputStream();
    DebugTextFile zaklady;


    ByteArrayOutputStream szkolyStream = new ByteArrayOutputStream();
    DebugTextFile szkoly;

    ByteArrayOutputStream liceaStream = new ByteArrayOutputStream();
    DebugTextFile licea;


    ByteArrayOutputStream bigUniversitiesStream = new ByteArrayOutputStream();
    DebugTextFile bigUniversities;

    ByteArrayOutputStream smallUniversitiesStream = new ByteArrayOutputStream();
    DebugTextFile smallUniversities;

    PdynExporter pdynExporter;
    private final RandomProvider randomProvider = new MockRandomProvider();

    @BeforeEach
    public void before() throws IOException {
        EngineConfiguration engineConfiguration = new EngineConfiguration();
        engineConfiguration.setStoreFactory(new TablesawStoreFactory());
        Board board = new Board(engineConfiguration, null, null, null);

        gd = new DebugTextFile(new PrintWriter(gdStream));
        agenci = new DebugTextFile(new PrintWriter(agenciStream));
        zaklady = new DebugTextFile(new PrintWriter(zakladyStream));
        szkoly = new DebugTextFile(new PrintWriter(szkolyStream));
        licea = new DebugTextFile(new PrintWriter(liceaStream));
        bigUniversities = new DebugTextFile(new PrintWriter(bigUniversitiesStream));
        smallUniversities = new DebugTextFile(new PrintWriter(smallUniversitiesStream));

        pdynExporter = new PdynExporter(datFileCreator,
                workDir,
                board,
                communeManager,
                true,
                idExporter,
                randomProvider);
        board.load(PdynExporter.class.getResourceAsStream("/pdyn15.csv"));

        when(communeManager.getCommunes()).thenReturn(List.of(
                new Commune("1412132", "a", Set.of(KilometerGridCell.fromLegacyPdynCoordinates(111,222))),
                new Commune("1061011", "b", Set.of(KilometerGridCell.fromLegacyPdynCoordinates(333,444))),
                new Commune("0810021", "c", Set.of(KilometerGridCell.fromLegacyPdynCoordinates(555,555)))
        ));
        doNothing().when(idExporter).saveToFile(anyString());
    }

    @Test
    @DisplayName("Should export data")
    public void execute() throws IOException {
        // given

        when(datFileCreator.createTextFile(Paths.get("dir", "gd.dat").toString())).thenReturn(gd);
        when(datFileCreator.createTextFile(Paths.get("dir", "agenci.dat").toString())).thenReturn(agenci);
        when(datFileCreator.createTextFile(Paths.get("dir", "zaklady.dat").toString())).thenReturn(zaklady);
        when(datFileCreator.createTextFile(Paths.get("dir","szkoly.dat").toString())).thenReturn(szkoly);
        when(datFileCreator.createTextFile(Paths.get("dir", "licea.dat").toString())).thenReturn(licea);
        when(datFileCreator.createTextFile(Paths.get("dir", "jew.dat").toString())).thenReturn(smallUniversities);
        when(datFileCreator.createTextFile(Paths.get("dir", "djew.dat").toString())).thenReturn(bigUniversities);

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
