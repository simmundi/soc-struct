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

package pl.edu.icm.board.agesex;

import net.snowyhollows.bento.config.WorkDir;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.household.model.AgeRange;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgeSexFromDistributionPickerTest {

    @Mock
    WorkDir workDir;

    @Test
    @DisplayName("Should give values in the correct range")
    void getRandomAge() {
        //given
        when(workDir.openForReading(any()))
                .thenReturn(AgeSexFromDistributionPickerTest.class.getResourceAsStream("/testAgeDistr.csv"));
        var ageSexFromDistributionPicker = new AgeSexFromDistributionPicker(workDir, "xxxx");
        List<Integer> results1 = new ArrayList<>();
        List<Integer> results2 = new ArrayList<>();

        //execute
        for (int i = 0; i < 5; i++){
            results1.add(ageSexFromDistributionPicker.getEmpiricalDistributedRandomAge(Person.Sex.M, AgeRange.AGE_10_14, 0));
            results2.add(ageSexFromDistributionPicker.getEmpiricalDistributedRandomAge(Person.Sex.K, AgeRange.AGE_15_19, 0));
        }

        //assert
        assertThat(results1).containsExactlyElementsOf(List.of(10, 10, 11, 11, 11));
        assertThat(results2).containsExactlyElementsOf(List.of(16, 17, 17, 19, 19));
    }
}
