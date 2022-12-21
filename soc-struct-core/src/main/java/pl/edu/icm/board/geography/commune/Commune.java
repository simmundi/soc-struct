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

package pl.edu.icm.board.geography.commune;

import pl.edu.icm.board.geography.KilometerGridCell;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Commune {
    private String teryt;
    private String name;
    private Set<KilometerGridCell> locations = new HashSet<>();

    public Commune(String teryt, String name, Set<KilometerGridCell> locations) {
        this.teryt = teryt;
        this.name = name;
        this.locations = locations;
    }

    public Commune(String teryt, String name) {
        this.teryt = teryt;
        this.name = name;
    }

    public Commune() {
    }

    public String getTeryt() {
        return teryt;
    }

    public void setTeryt(String teryt) {
        this.teryt = teryt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<KilometerGridCell> getCells() {
        return locations;
    }

    @Override
    public String toString() {
        return "Commune{" +
                "teryt='" + teryt + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Commune commune = (Commune) o;
        return teryt.equals(commune.teryt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teryt);
    }
}
