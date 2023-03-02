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

package pl.edu.icm.em.common.math.histogram;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HistogramTest {

    private enum Color {
        VIOLET, YELLOW, PINK, GOLD
    }

    @Test
    @DisplayName("Should add bins to a pool and count correct total")
    public void add() {
        // given
        Histogram<Color> histogram = new Histogram<>();
        Bin<Color> violets = histogram.add(Color.VIOLET, 123);
        histogram.add(Color.YELLOW, 123);

        // execute
        int countBefore = histogram.getTotalCount();
        violets.pick();
        int countAfter = histogram.getTotalCount();

        // assert
        assertEquals(246, countBefore);
        assertEquals(245, countAfter);
    }

    @Test
    @DisplayName("Should create subpools from a pool")
    public void createSubPool() {
        // given
        Histogram<Color> histogram = new Histogram<>();
        Bin<Color> violets = histogram.add(Color.VIOLET, 10);
        Bin<Color> yellows = histogram.add(Color.YELLOW, 10);
        Bin<Color> golds = histogram.add(Color.GOLD, 10);
        Bin<Color> pinks = histogram.add(Color.PINK, 10);

        int totalCount = histogram.getTotalCount();
        Histogram<Color> sub1 = histogram.createSubHistogram(Color.VIOLET, Color.YELLOW, Color.GOLD);
        Histogram<Color> sub2 = histogram.createSubHistogram(Color.YELLOW, Color.GOLD, Color.PINK);

        assertThat(totalCount).isEqualTo(40);
        assertThat(sub1.getTotalCount()).isEqualTo(30);
        assertThat(sub2.getTotalCount()).isEqualTo(30);
    }

    @Test
    @DisplayName("Should sample bins")
    public void sample() {
        // given
        Histogram<Color> histogram = new Histogram<>();
        histogram.add(Color.VIOLET, 3);
        histogram.add(Color.YELLOW, 3);


        // execute
        List<Color> results = new ArrayList<>();
        while (histogram.getTotalCount() > 0) {
            results.add(histogram.sample(0.5).pick());
        }

        // assert
        assertThat(results).containsExactly(
                Color.YELLOW, Color.VIOLET,
                Color.YELLOW, Color.VIOLET,
                Color.YELLOW, Color.VIOLET
        );
    }

    @Test
    public void reset() {
        // given
        Histogram<Color> histogram = new Histogram<>();
        histogram.add(Color.VIOLET, 5);
        histogram.add(Color.GOLD, 3);
        histogram.add(Color.PINK, 2);

        // execute
        histogram.sample(0).pick(10);
        int countBefore = histogram.getTotalCount();
        histogram.sample(0).pick();
        int countAfter = histogram.getTotalCount();

        // assert
        assertThat(countBefore).isEqualTo(0);
        assertThat(countAfter).isEqualTo(9);
    }

    @Test
    void streamBins() {
        // given
        Histogram<Color> histogram = new Histogram<>();
        histogram.add(Color.VIOLET, 5);
        histogram.add(Color.GOLD, 3);
        histogram.add(Color.PINK, 2);

        // execute
        List<Bin<Color>> bins = histogram.streamBins().collect(Collectors.toList());

        // assert
        assertThat(bins)
                .extracting(Bin::getLabel, Bin::getCount)
                .containsExactly(
                        tuple(Color.VIOLET, 5),
                        tuple(Color.GOLD, 3),
                        tuple(Color.PINK, 2));
    }

    @Test
    void sampleNth() {
        // given
        Histogram<Color> histogram = new Histogram<>();
        histogram.add(Color.VIOLET, 5);
        histogram.add(Color.GOLD, 3);
        histogram.add(Color.PINK, 2);

        // execute
        List<Bin<Color>> bins = IntStream.range(0, 10)
                .mapToObj(n -> histogram.sampleNth(n))
                .collect(Collectors.toList());

        // assert
        assertThat(bins)
                .extracting(Bin::getLabel)
                .containsExactly(
                        Color.VIOLET, Color.VIOLET, Color.VIOLET, Color.VIOLET, Color.VIOLET,
                        Color.GOLD, Color.GOLD, Color.GOLD,
                        Color.PINK, Color.PINK);
    }
}
