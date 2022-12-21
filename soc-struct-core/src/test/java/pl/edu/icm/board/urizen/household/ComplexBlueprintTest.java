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

package pl.edu.icm.board.urizen.household;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.geography.prg.model.AddressPoint;
import pl.edu.icm.board.urizen.household.model.ComplexBlueprint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplexBlueprintTest {

    @Mock
    private AddressPoint addressPoint;

    @BeforeEach
    void setup() {
        when(addressPoint.getEasting()).thenReturn(1.23f);
        when(addressPoint.getNorthing()).thenReturn(4.56f);
    }

    @Test
    void getLocationAndFrom() {
//        given
        var complexBlueprint = ComplexBlueprint.from(addressPoint);
//        execute
        var location = complexBlueprint.getLocation();
//        assert
        assertThat(location.getE()).isEqualTo(1);
        assertThat(location.getN()).isEqualTo(4);
    }

    @Test
    void getSizeAndAddAndGetHouseholdsId() {
//        given
        var complexBlueprint = ComplexBlueprint.from(addressPoint);
        for (var i = 0; i < 10; i++) {
            complexBlueprint.addHouseholdId(i);
        }
//        execute
        var householdsIds = complexBlueprint.getHouseholdsId();
//        assert
        assertThat(householdsIds).containsExactly(0,1,2,3,4,5,6,7,8,9);
        assertThat(complexBlueprint.getSize()).isEqualTo(10);
    }

}
