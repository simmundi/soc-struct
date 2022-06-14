package pl.edu.icm.board.model;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;
import pl.edu.icm.trurl.ecs.Entity;

import java.util.ArrayList;
import java.util.List;

@WithMapper(namespace = "complex")
public class Complex {
    private List<Entity> households = new ArrayList<>();
    private int size;
    private Type type;

    public Complex() {
    }

    public Complex(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<Entity> getHouseholds() {
        return households;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        CLERGY_HOUSE,
        NURSING_HOME,
        DORM,
        PRISON,
        MONASTERY,
        BARRACKS,
        RESIDENTIAL_HOUSE
    }
}
