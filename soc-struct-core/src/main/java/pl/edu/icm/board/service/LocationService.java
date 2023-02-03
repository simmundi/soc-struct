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

package pl.edu.icm.board.service;

import org.checkerframework.checker.signature.qual.DotSeparatedIdentifiersOrPrimitiveType;
import pl.edu.icm.board.model.Attendee;
import pl.edu.icm.board.model.EducationalInstitution;
import pl.edu.icm.board.model.Employee;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.trurl.ecs.Entity;

import java.util.Optional;

public class LocationService {

    public Entity getPrimaryCommuteTarget(Entity personEntity) {
        Employee employee = personEntity.get(Employee.class);
        if (employee != null) {
            return employee.getWork();
        }
        Attendee attendee = personEntity.get(Attendee.class);
        if (attendee != null) {
            Entity institution = attendee.getInstitution();
            if (institution.get(EducationalInstitution.class).getLevel().isUniversity()) {
                return institution;
            }
        }
        return null;
    }

    public Location getPrimaryCommuteTargetLocation(Entity personEntity) {
        Entity primaryCommuteTarget = getPrimaryCommuteTarget(personEntity);
        return primaryCommuteTarget != null ? primaryCommuteTarget.get(Location.class) : null;
    }

    public Entity getEducationInstitution(Entity personEntity) {
        Attendee attendee = personEntity.get(Attendee.class);
        if (attendee != null) {
            Entity institution = attendee.getInstitution();
            if (institution != null && !institution.get(EducationalInstitution.class).getLevel().isUniversity()) {
                return institution;
            }
        }
        return null;
    }
}
