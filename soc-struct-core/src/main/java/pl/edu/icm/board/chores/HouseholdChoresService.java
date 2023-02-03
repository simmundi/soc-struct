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

package pl.edu.icm.board.chores;

import pl.edu.icm.board.model.Chores;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.trurl.ecs.Entity;

import java.util.HashSet;
import java.util.Set;

public class HouseholdChoresService {

    public float calculateCosts(Entity householdEntity) {
        Household household = householdEntity.get(Household.class);
        Location householdLocation = householdEntity.get(Location.class);
        if (household == null) {
            throw new IllegalArgumentException("Entity is not a household");
        }
        if (householdLocation == null) {
            throw new IllegalArgumentException("Household has no location");
        }


        Set<Location> kidLocations = new HashSet<>();

        for (Entity memberEntity : household.getMembers()) {
            Entity kidLocation = extractKidLocation(memberEntity);
            if (kidLocation != null) {
                kidLocations.add(kidLocation.get(Location.class));
            }
        }

        double totalCost = 0;

        Set<Location> adultLocations = new HashSet<>();
        for (Entity memberEntity : household.getMembers()) {
            Chores chores = memberEntity.get(Chores.class);
            if (chores != null && chores.hasChore(Chores.Chore.WALKING_KIDS_TO_SCHOOL)) {
                Location location = primaryLocation
            } else {
                Location location = extractTargetLocation(memberEntity);
                if (location != null) {

                }
            }
        }
    }
}
