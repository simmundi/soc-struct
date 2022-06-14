package pl.edu.icm.board.geography.gis;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class TerytGridItem {
    private String teryt;
    private String name;
    private int n;
    private int e;

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

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getE() {
        return e;
    }

    public void setE(int e) {
        this.e = e;
    }
}
