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

@WithMapper(namespace = "area")
public class Area {
    private short e;
    private short n;

    public Area() {
    }

    public Area(short e, short n) {
        this.e = e;
        this.n = n;
    }

    public short getN() {
        return n;
    }

    public short getE() {
        return e;
    }


    public void setE(short e) {
        this.e = e;
    }

    public void setN(short n) {
        this.n = n;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Area that = (Area) o;
        return e == that.e &&
                n == that.n;
    }

    @Override
    public int hashCode() {
        return Objects.hash(e, n);
    }

    @Override
    public String toString() {
        return "Area{" +
                "e=" + e +
                ", n=" + n +
                '}';
    }
}
