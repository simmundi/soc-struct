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

package pl.edu.icm.board.geography.density;

import com.univocity.parsers.common.record.Record;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.util.BoardCsvLoader;
import pl.edu.icm.trurl.ecs.util.Selectors;

import java.io.FileNotFoundException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PopulationDensityLoaderTest {

    @Mock
    Selectors selectors;
    @Mock
    BoardCsvLoader boardCsvLoader;

    @Mock
    Board board;

    @Mock
    Record recordA;
    @Mock
    Record recordB;

    @Test
    @DisplayName("Should load the data from csv")
    public void load() throws FileNotFoundException {
        // given
        when(boardCsvLoader.stream("test")).thenReturn(List.of(recordA));
        when(recordA.getString("id_oczka")).thenReturn("N12E12");
        PopulationDensityLoader populationDensityLoader = new PopulationDensityLoader(boardCsvLoader, "test", board, selectors);

        // when
        populationDensityLoader.load();

        // assert
        Mockito.verify(boardCsvLoader).stream(anyString());
    }

    @Test
    @DisplayName("Should correctly sample cells")
    public void sample() throws FileNotFoundException {
        // given
        when(boardCsvLoader.stream("test")).thenReturn(List.of(recordA, recordB));
        when(recordA.getString("id_oczka")).thenReturn("N1E1");
        when(recordB.getString("id_oczka")).thenReturn("N2E2");
        PopulationDensityLoader populationDensityLoader = new PopulationDensityLoader(boardCsvLoader, "test", board, selectors);
        populationDensityLoader.load();

        // when
        var resultA = populationDensityLoader.sample(0);
        var resultB = populationDensityLoader.sample(0.9);

        // assert
        assertThat(resultA).isEqualTo(KilometerGridCell.fromIdOczkaGus("N1E1"));
        assertThat(resultB).isEqualTo(KilometerGridCell.fromIdOczkaGus("N2E2"));
    }

    @Test
    @DisplayName("Should recognize the populated cells")
    public void isPopulated() throws FileNotFoundException {
        // given
        when(boardCsvLoader.stream("test")).thenReturn(List.of(recordA, recordB));
        when(recordA.getString("id_oczka")).thenReturn("N1E1");
        when(recordB.getString("id_oczka")).thenReturn("N2E2");
        PopulationDensityLoader populationDensityLoader = new PopulationDensityLoader(boardCsvLoader, "test", board, selectors);
        populationDensityLoader.load();

        // when
        var resultA = populationDensityLoader.isPopulated(KilometerGridCell.fromIdOczkaGus("N1E1"));
        var resultB = populationDensityLoader.isPopulated(KilometerGridCell.fromIdOczkaGus("N2E2"));
        var resultC = populationDensityLoader.isPopulated(KilometerGridCell.fromIdOczkaGus("N3E3"));

        // assert
        assertThat(resultA).isTrue();
        assertThat(resultB).isTrue();
        assertThat(resultC).isFalse();
    }
}
