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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShelfTest {

    @Test
    public void test() {
        Histogram.Shelf<String> shelf = new Histogram.Shelf<>(5);

        for (int i = 0; i < 25; i++) {
            shelf.add(bin("bin " + i, 1));
        }

        int total = shelf.getTotal();

        assertThat(total).isEqualTo(25);
    }

    private Bin<String> bin(String label, int count) {
        return new Bin<>(label, count);
    }
}
