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

package pl.edu.icm.board.model;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

import java.util.Objects;

@WithMapper
public class Location {
    private int e;
    private int n;

    public Location() {
    }

    public Location(int e, int n) {
        this.e = e;
        this.n = n;
    }

    public int getN() {
        return n;
    }

    public int getE() {
        return e;
    }

    public void moveByMeters(int metersE, int metersN) {
        e += metersE;
        n += metersN;
    }

    public static Location fromPl1992MeterCoords(float eastingMeters, float northingMeters) {
        Location location = new Location();
        location.n = (int) northingMeters;
        location.e = (int) eastingMeters;
        return location;
    }

    public void setE(int e) {
        this.e = e;
    }

    public void setN(int n) {
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
