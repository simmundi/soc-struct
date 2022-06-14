package pl.edu.icm.board.model;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class EducationalInstitution {
    private EducationLevel level;
    private int pupilCount;
    private short teacherCount;

    public EducationalInstitution() {
    }

    public EducationalInstitution(EducationLevel level, int pupilCount, int teacherCount) {
        this.level = level;
        this.pupilCount = pupilCount;
        this.teacherCount = (short)teacherCount;
    }

    public EducationLevel getLevel() {
        return level;
    }

    public void setLevel(EducationLevel level) {
        this.level = level;
    }

    public int getPupilCount() {
        return pupilCount;
    }

    public void setPupilCount(int pupilCount) {
        this.pupilCount = pupilCount;
    }

    public short getTeacherCount() {
        return teacherCount;
    }

    public void setTeacherCount(short teacherCount) {
        this.teacherCount = teacherCount;
    }
}
