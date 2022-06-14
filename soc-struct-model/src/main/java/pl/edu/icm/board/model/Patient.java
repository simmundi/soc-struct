package pl.edu.icm.board.model;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class Patient {
    private Entity healthcare;

    public Entity getHealthcare() {
        return healthcare;
    }

    public void setHealthcare(Entity healthcare) {
        this.healthcare = healthcare;
    }
}
