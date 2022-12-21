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

package pl.edu.icm.board.urizen.replicants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Replicant;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReplicantPrototypesTest {
    @Mock
    private Session session;
    @Mock
    private Entity entity;
    @InjectMocks
    ReplicantPrototypes replicantPrototypes;
    @Mock
    private List<Entity> complexHouseholds;

    @Test
    @DisplayName("Should create an entity with Person adn Replicant components")
    void nursingHomeResident() {
        // given
        Person person = new Person();
        person.setSex(Person.Sex.M);
        person.setAge(13);
        when(session.createEntity()).thenReturn(entity);
        when(entity.add(any())).thenAnswer(args -> args.getArgument(0));

        // execute
        replicantPrototypes.nursingHomeResident(session, person.getSex(), person.getAge());

        // assert
        verify(entity).add(eq(person));
        verify(entity).add(Mockito.any(Replicant.class));
        verify(entity, times(2)).add(any());
    }

    @Test
    @DisplayName("Should create an entity Household, Location and Replicant components")
    void clergyHouseRoom() {
        // given
        Location location = Location.fromPl1992MeterCoords(10000, 45000);
        when(session.createEntity()).thenReturn(entity);
        when(entity.add(any())).thenAnswer(args -> args.getArgument(0));

        // execute
        replicantPrototypes.clergyHouseRoom(session, KilometerGridCell.fromLocation(location));

        // assert
        verify(entity, times(3)).add(any());
        verify(entity).add(Mockito.any(Household.class));
        verify(entity).add(Mockito.any(Replicant.class));
    }
}
