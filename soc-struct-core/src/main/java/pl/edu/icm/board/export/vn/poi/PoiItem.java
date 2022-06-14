package pl.edu.icm.board.export.vn.poi;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;
import pl.edu.icm.trurl.visnow.VnCoords;

@WithMapper
public class PoiItem implements VnCoords {
    private float x;
    private float y;
    private Type subsets;
    private int slots;
    private int taken;

    @Override
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    @Override
    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public Type getSubsets() {
        return subsets;
    }

    public void setSubsets(Type subsets) {
        this.subsets = subsets;
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    public int getTaken() {
        return taken;
    }

    public void setTaken(int taken) {
        this.taken = taken;
    }

    public enum Type {
        CLERGY_HOUSE,
        NURSING_HOME,
        DORM,
        PRISON,
        MONASTERY,
        BARRACKS,

        EDU_PRESCHOOL,
        EDU_PRIMARY,
        EDU_HIGH,
        EDU_PRIMARY_AND_HIGH,
        EDU_UNIVERSITY,

        WORKPLACE,

        HEALTHCARE_POZ,
        HEALTHCARE_OTHER
    }
}
