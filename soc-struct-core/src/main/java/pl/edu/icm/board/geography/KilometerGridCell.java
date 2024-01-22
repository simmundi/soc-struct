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

package pl.edu.icm.board.geography;

import pl.edu.icm.em.socstruct.component.geo.Area;
import pl.edu.icm.em.socstruct.component.geo.Location;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class KilometerGridCell {
    private static final Pattern FORMATTED = Pattern.compile("^N(\\d+)E(\\d+)$");

    private final int e;
    private final int n;

    public KilometerGridCell(int e, int n) {
        this.e = e;
        this.n = n;
    }

    public int getE() {
        return e;
    }

    public int getN() {
        return n;
    }

    public KilometerGridCell neighbourN() {
        return new KilometerGridCell(e, n + 1);
    }

    public KilometerGridCell neighbourS() {
        return new KilometerGridCell(e, n - 1);
    }

    public KilometerGridCell neighbourE() {
        return new KilometerGridCell(e + 1, n);
    }

    public KilometerGridCell neighbourW() {
        return new KilometerGridCell(e - 1, n);
    }

    public Stream<KilometerGridCell> neighboringChebyshevCircle(int r) {
        final int side = r * 2 + 1;
        return IntStream.range(0, side * side).mapToObj(idx -> {
            int dy = (idx / side) - r;
            int dx = (idx % side) - r;
            return new KilometerGridCell(e + dx, n + dy);
        });
    }

    public Stream<KilometerGridCell> neighboringCircle(int r) {
        return neighboringChebyshevCircle(r)
                .filter(cell -> Math.hypot(e - cell.e, n - cell.n) <= r);
    }

    @Override
    public String toString() {
        return String.format("N%dE%d", n, e);
    }

    public static KilometerGridCell fromLegacyPdynCoordinates(int col, int row) {
        return new KilometerGridCell(
                col + 71,
                875 - row);
    }

    public int getLegacyPdynCol() {
        return getE() - 71;
    }

    public static KilometerGridCell fromIdOczkaGus(String idOczka) {
        Matcher matcher = FORMATTED.matcher(idOczka);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(idOczka + " is not a valid gusowski identyfikator oczka");
        }
        return new KilometerGridCell(
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(1))
        );
    }

    public static KilometerGridCell fromPl1992ENMeters(float e, float n) {
        return new KilometerGridCell(
                (int) (e / 1000), (int) (n / 1000)
        );
    }

    public static KilometerGridCell fromPl1992ENKilometers(int e, int n) {
        return new KilometerGridCell(e, n);
    }

    public static KilometerGridCell fromLocation(Location location) {
        return new KilometerGridCell((int) (location.getE() / 1000), (int) (location.getN() / 1000));
    }

    public static KilometerGridCell fromArea(Area area) {
        return new KilometerGridCell(area.getE(), area.getN());
    }

    public int getLegacyPdynRow() {
        return 875 - getN();
    }

    public Location toLocation() {
        int locationN = n * 1000 + 500;
        int locationE = e * 1000 + 500;
        return new Location(locationE, locationN);
    }

    public Area toArea() {
        return new Area((short) e, (short) n);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KilometerGridCell kilometerGridCell = (KilometerGridCell) o;
        return e == kilometerGridCell.e &&
                n == kilometerGridCell.n;
    }

    @Override
    public int hashCode() {
        return Objects.hash(e, n);
    }
}
