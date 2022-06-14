package pl.edu.icm.board.export;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;
import pl.edu.icm.trurl.visnow.VnCoords;

@WithMapper
public class ExportedPatient implements VnCoords {
    float x;
    float y;
    float distance;
    short sex;
    short patientType;

    public short getSex() {
        return sex;
    }

    public void setSex(short sex) {
        this.sex = sex;
    }

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

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public void setType(short patientType) {
        this.patientType = patientType;
    }

    public short getType() {
        return patientType;
    }

}
