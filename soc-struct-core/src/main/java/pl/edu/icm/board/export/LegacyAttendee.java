package pl.edu.icm.board.export;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;
import pl.edu.icm.trurl.ecs.Entity;

@WithMapper
public class LegacyAttendee {
    private Entity place;

    public Entity getPlace() {
        return place;
    }

    public void setPlace(Entity place) {
        this.place = place;
    }
}
