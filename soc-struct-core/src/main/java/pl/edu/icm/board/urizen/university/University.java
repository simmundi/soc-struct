package pl.edu.icm.board.urizen.university;

import pl.edu.icm.board.model.Location;

public class University {
    protected final Location location;
    protected final int studentCount;


    public University(Location location, int studentCount) {
        this.location = location;
        this.studentCount = studentCount;
    }

    public Location getLocation() {
        return location;
    }

    public int getStudentCount() {
        return studentCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        University that = (University) o;
        return studentCount == that.studentCount && location.equals(that.location);
    }

}
