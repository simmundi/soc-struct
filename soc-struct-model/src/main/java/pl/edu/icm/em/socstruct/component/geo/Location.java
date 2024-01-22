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

package pl.edu.icm.em.socstruct.component.geo;

import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

import java.util.Objects;

@WithDao
public class Location {
    private float e;
    private float n;

    public Location() {
    }

    public Location(int e, int n) {
        this.e = e;
        this.n = n;
    }

    public float getN() {
        return n;
    }

    public float getE() {
        return e;
    }

    public void moveByMeters(float metersE, float metersN) {
        e += metersE;
        n += metersN;
    }

    public static Location fromEquiarealENMeters(float eastingMeters, float northingMeters) {
        Location location = new Location();
        location.n = (int) northingMeters;
        location.e = (int) eastingMeters;
        return location;
    }

    public void setE(float e) {
        this.e = e;
    }

    public void setN(float n) {
        this.n = n;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location that = (Location) o;
        return e == that.e &&
                n == that.n;
    }

    @Override
    public int hashCode() {
        return Objects.hash(e, n);
    }
}
