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

package pl.edu.icm.board.geography.prg.model;

import pl.edu.icm.em.socstruct.component.geo.Location;
import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

@WithDao
public class IndexedAddressPoint {
    private String normalized;
    private String normalizedStreetName;
    private AddressPoint addressPoint;
    private Location location;

    public IndexedAddressPoint() {
    }

    public IndexedAddressPoint(String normalized, String normalizedStreetName, AddressPoint addressPoint, Location location) {
        this.normalized = normalized;
        this.normalizedStreetName = normalizedStreetName;
        this.addressPoint = addressPoint;
    }

    public void setNormalized(String normalized) {
        this.normalized = normalized;
    }

    public void setNormalizedStreetName(String normalizedStreetName) {
        this.normalizedStreetName = normalizedStreetName;
    }

    public void setAddressPoint(AddressPoint addressPoint) {
        this.addressPoint = addressPoint;
    }

    public String getNormalized() {
        return normalized;
    }

    public String getNormalizedStreetName() {
        return normalizedStreetName;
    }

    public AddressPoint getAddressPoint() {
        return addressPoint;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
