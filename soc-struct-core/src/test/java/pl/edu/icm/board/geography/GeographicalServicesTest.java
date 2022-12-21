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

package pl.edu.icm.board.geography;

import org.junit.jupiter.api.Test;
import pl.edu.icm.board.model.Area;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class GeographicalServicesTest {

    @Test
    void convertAreaToKilometerGridCell() {
        //given
        var geographicalServices = new GeographicalServices();
        var area = new Area((short) 100, (short) 150);
        //execute
        var kgc = geographicalServices.convertAreaToKilometerGridCell(area);
        //assert
        assertThat(kgc).isEqualTo(KilometerGridCell.fromArea(area));
    }
}