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
