package pl.edu.icm.board.urizen.place.export;

import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.urizen.place.EducationalInstitutionFromCsv;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;
import pl.edu.icm.trurl.visnow.VnCoords;

@WithMapper
public class ExportedToCsvEducationalInstitution implements VnCoords {
    float x;
    float y;
    short type;
    short pupils;

    public short getTeachers() {
        return teachers;
    }

    public void setTeachers(short teachers) {
        this.teachers = teachers;
    }

    short teachers;

    public ExportedToCsvEducationalInstitution() {
    }

    public ExportedToCsvEducationalInstitution(Location location, EducationalInstitutionFromCsv institution) {
        this.x = location.getE() / 1000f;
        this.y = location.getN() / 1000f;
        this.pupils = (short) institution.getPupils();
        this.teachers = (short) institution.getTeachers();
        if (institution.getLevel() == null) {
            type = 0;
        } else {
            switch (institution.getLevel()) {
                case PRESCHOOL:
                    type = 1; break;
                case PRIMARY:
                    type = 2; break;
                case PRIMARY_AND_HIGH:
                    type = 3; break;
                case HIGH:
                    type = 4; break;
                case ADULTS:
                    type = 5; break;
                default:
                    type = 0;
            }
        }
    }

    public short getPupils() {
        return pupils;
    }

    public void setPupils(short pupils) {
        this.pupils = pupils;
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

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }
}
