package pl.edu.icm.board.model;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper(namespace = "healthcare")
public class Healthcare {
    HealthcareType type;
    int capacity;
    int id;

    public HealthcareType getType() {
        return type;
    }

    public void setType(HealthcareType type) {
        this.type = type;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
