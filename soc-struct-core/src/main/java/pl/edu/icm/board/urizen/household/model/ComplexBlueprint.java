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

package pl.edu.icm.board.urizen.household.model;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import pl.edu.icm.board.geography.prg.model.AddressPoint;
import pl.edu.icm.em.socstruct.component.geo.Location;

import java.util.List;

public class ComplexBlueprint {
    private final Location location;
    private final List<Integer> householdsId = new IntArrayList();

    private ComplexBlueprint(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public List<Integer> getHouseholdsId() {
        return householdsId;
    }

    public static ComplexBlueprint from(Location location) {
        return new ComplexBlueprint(location);
    }

    public void addHouseholdId(int householdId) {
        householdsId.add(householdId);
    }

    public int getSize() {
        return householdsId.size();
    }
}
