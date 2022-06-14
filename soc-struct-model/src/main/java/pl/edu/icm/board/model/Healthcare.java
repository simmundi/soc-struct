package pl.edu.icm.board.model;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper(namespace = "healthcare")
public class Healthcare {
    HealthcareType type;

    public HealthcareType getType() {
        return type;
    }

    public void setType(HealthcareType type) {
        this.type = type;
    }
}
