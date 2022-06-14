package pl.edu.icm.board.model;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class Replicant {
    ReplicantType type;

    public ReplicantType getType() {
        return type;
    }

    public void setType(ReplicantType type) {
        this.type = type;
    }
}
