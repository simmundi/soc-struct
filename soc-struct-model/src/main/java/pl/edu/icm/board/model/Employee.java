package pl.edu.icm.board.model;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;
import pl.edu.icm.trurl.ecs.Entity;

@WithMapper
public class Employee {
    private Entity work;

    public Entity getWork() {
        return work;
    }

    public void setWork(Entity work) {
        this.work = work;
    }
}
