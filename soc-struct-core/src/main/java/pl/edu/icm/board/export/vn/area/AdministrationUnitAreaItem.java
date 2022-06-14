package pl.edu.icm.board.export.vn.area;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class AdministrationUnitAreaItem {
    private short commune;

    public AdministrationUnitAreaItem() {
    }

    public short getCommune() {
        return commune;
    }

    public void setCommune(short commune) {
        this.commune = commune;
    }
}
