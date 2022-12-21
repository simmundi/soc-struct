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

package pl.edu.icm.board.urizen.university;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.board.geography.KilometerGridCell;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
class UniversityLoaderTest {

    @Test
    @DisplayName("Should correctly parse university definition file")
    void load() {
        //given
        var textFileLines = List.of("2",
                "3 2 1 0",
                "100 80 Uni1",
                "1 4",
                "10 10 Uni2");
        //execute
        UniversityLoader loader = new UniversityLoader("big", "small", null);
        var universityList = loader.load(textFileLines);
        //assert
        assertThat(universityList)
                .contains(
                        new University(
                                KilometerGridCell.fromLegacyPdynCoordinates(80, 100).toLocation(), 3
                        ),
                        new University(
                                KilometerGridCell.fromLegacyPdynCoordinates(10, 10).toLocation(), 1)
                );

    }

    @Test
    @DisplayName("Should correctly parse university definition file")
    void parseUniversityDefinition() {
        //given
        String universityStudents = "3 12 4 0";
        String locationAndName = "5 3 GloriousUniversity";
        String locationNoName = "1 3";

        //execute
        UniversityLoader loader = new UniversityLoader("big", "small", null);
        University bigUniversity = loader.parseUniversityDefinition(universityStudents, locationAndName);
        University smallUniversity = loader.parseUniversityDefinition(universityStudents, locationNoName);

        //assert
        assertThat(bigUniversity.studentCount).isEqualTo(3);
        assertThat(KilometerGridCell.fromLocation(bigUniversity.getLocation()).getLegacyPdynRow()).isEqualTo(5);
        assertThat(KilometerGridCell.fromLocation(bigUniversity.getLocation()).getLegacyPdynCol()).isEqualTo(3);

        assertThat(smallUniversity.studentCount).isEqualTo(3);
        assertThat(KilometerGridCell.fromLocation(smallUniversity.getLocation()).getLegacyPdynRow()).isEqualTo(1);
        assertThat(KilometerGridCell.fromLocation(smallUniversity.getLocation()).getLegacyPdynCol()).isEqualTo(3);
    }
}
