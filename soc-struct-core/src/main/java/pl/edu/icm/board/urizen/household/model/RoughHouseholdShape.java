package pl.edu.icm.board.urizen.household.model;

import java.util.Objects;

public class RoughHouseholdShape {
    private final int inhabitantsCount;
    private final boolean flag70Plus;

    public RoughHouseholdShape(int inhabitantsCount, boolean flag70Plus) {
        this.inhabitantsCount = inhabitantsCount;
        this.flag70Plus = flag70Plus;
    }

    public int getInhabitantsCount() {
        return inhabitantsCount;
    }

    public boolean isFlag70Plus() {
        return flag70Plus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoughHouseholdShape that = (RoughHouseholdShape) o;
        return inhabitantsCount == that.inhabitantsCount &&
                flag70Plus == that.flag70Plus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(inhabitantsCount, flag70Plus);
    }
}
