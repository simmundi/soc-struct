package pl.edu.icm.board.model;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class AdministrationUnit {
    private String teryt;

    public AdministrationUnit(String teryt) {
        this.teryt = teryt;
    }

    public AdministrationUnit() {
    }

    public String getTeryt() {
        return teryt;
    }

    public void setTeryt(String teryt) {
        this.teryt = teryt;
    }
}
