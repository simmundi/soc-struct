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

package pl.edu.icm.board.urizen.household.cloner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.EntityMocker;
import pl.edu.icm.board.agesex.AgeSexFromDistributionPicker;
import pl.edu.icm.em.socstruct.component.geo.AdministrationUnitTag;
import pl.edu.icm.em.socstruct.component.Household;
import pl.edu.icm.em.socstruct.component.Person;
import pl.edu.icm.trurl.bin.Bin;
import pl.edu.icm.trurl.ecs.Engine;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FamilyShapeStatsServiceTest {

    @Mock
    EngineIo engineIo;

    @Mock
    Engine engine;

    @InjectMocks
    FamilyShapeStatsService service;

    @Mock
    AgeSexFromDistributionPicker ageSexFromDistributionPicker;

    private EntityMocker mock;

    @BeforeEach
    void configure() {
        mock = new EntityMocker(null, Person.class, AdministrationUnitTag.class, Household.class);

        when(engineIo.getEngine()).thenReturn(engine);
        when(engine.streamDetached()).thenReturn(Stream.of(
                // młode małżeństwo
                mock.entity(1, mock.person(27, Person.Sex.F)),
                mock.entity(2, mock.person(27, Person.Sex.M)),
                mock.entity(1001, mock.household(1, 2), mock.au("warszawa")),

                // młode małżeństwo
                mock.entity(3, mock.person(28, Person.Sex.F)),
                mock.entity(4, mock.person(28, Person.Sex.F)),
                mock.entity(1002, mock.household(3, 4), mock.au("warszawa")),

                // singiel
                mock.entity(5, mock.person(56, Person.Sex.F)),
                mock.entity(1003, mock.household(5), mock.au("warszawa")),

                // emeryt
                mock.entity(6, mock.person(80, Person.Sex.M)),
                mock.entity(1004, mock.household(6), mock.au("warszawa")),

                // emeryt
                mock.entity(7, mock.person(69, Person.Sex.F)),
                mock.entity(1005, mock.household(7), mock.au("kraków")),

                // małżeństwo z dzieckiem
                mock.entity(8, mock.person(30, Person.Sex.F)),
                mock.entity(9, mock.person(30, Person.Sex.F)),
                mock.entity(10, mock.person(7, Person.Sex.F)),
                mock.entity(1006, mock.household(8, 9, 10), mock.au("kraków")),

                // małżeństwo z dzieckiem
                mock.entity(11, mock.person(31, Person.Sex.F)),
                mock.entity(12, mock.person(32, Person.Sex.F)),
                mock.entity(13, mock.person(9, Person.Sex.F)),
                mock.entity(1007, mock.household(11, 12, 13), mock.au("kraków"))
        ));
    }

    @Test
    @DisplayName("Should create proper aggregates for household shapes")
    @Disabled("TODO: fix testing apparatus, broken after refactors")
    void countStats() {

        // execute
        var result = service.countStats();

        // assert
        assertThat(result.populationByTeryt.get("kraków").get()).isEqualTo(7);
        assertThat(result.populationByTeryt.get("warszawa").get()).isEqualTo(6);
        assertThat(result.shapesByTeryt.get("warszawa").streamBins()).extracting(
                Bin::getLabel,
                Bin::getCount
        ).containsExactlyInAnyOrder(
                // dwa młode małżeństwa
                tuple(HouseholdShape.tryCreate(mock.id(1001), ageSexFromDistributionPicker), 2),
                // singiel
                tuple(HouseholdShape.tryCreate(mock.id(1003), ageSexFromDistributionPicker), 1),
                // emeryt
                tuple(HouseholdShape.tryCreate(mock.id(1004), ageSexFromDistributionPicker), 1)
        );
        assertThat(result.shapesByTeryt.get("kraków").streamBins()).extracting(
                Bin::getLabel,
                Bin::getCount
        ).containsExactlyInAnyOrder(
                // emeryt
                tuple(HouseholdShape.tryCreate(mock.id(1005), ageSexFromDistributionPicker), 1),
                // dwa małżeństwa z dzieckiem
                tuple(HouseholdShape.tryCreate(mock.id(1006), ageSexFromDistributionPicker), 2)
        );
    }
}
