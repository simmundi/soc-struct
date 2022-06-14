package pl.edu.icm.board.export;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;
import pl.edu.icm.trurl.visnow.VnCoords;

@WithMapper
public class ExportedEducationalInstitution implements VnCoords {
    private short id;
    private float x;
    private float y;
    private int capacity;
    private short leftEmpty;
    private short type;
    private short precision;

    public short getPrecision() {
        return precision;
    }

    public void setPrecision(short precision) {
        this.precision = precision;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public short getLeftEmpty() {
        return leftEmpty;
    }

    public void setLeftEmpty(short leftEmpty) {
        this.leftEmpty = leftEmpty;
    }
}
