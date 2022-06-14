package pl.edu.icm.board.model;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;
import pl.edu.icm.trurl.ecs.Entity;

@WithMapper
public class Attendee {
    private Entity institution;
    private Entity secondaryInstitution;

    public Entity getInstitution() {
        return institution;
    }

    public void setInstitution(Entity institution) {
        this.institution = institution;
    }

    public Entity getSecondaryInstitution() {
        return secondaryInstitution;
    }

    public void setSecondaryInstitution(Entity secondaryInstitution) {
        this.secondaryInstitution = secondaryInstitution;
    }

}
