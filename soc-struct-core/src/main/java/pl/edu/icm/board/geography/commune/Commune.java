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
