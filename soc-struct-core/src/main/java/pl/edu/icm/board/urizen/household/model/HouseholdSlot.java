package pl.edu.icm.board.urizen.household.model;

import pl.edu.icm.board.geography.KilometerGridCell;

import java.util.Objects;

public class HouseholdSlot {
    private final RoughHouseholdShape shape;
    private final KilometerGridCell cell;

    public HouseholdSlot(RoughHouseholdShape shape, KilometerGridCell cell) {
        this.shape = shape;
        this.cell = cell;
    }

    public RoughHouseholdShape getShape() {
        return shape;
    }

    public KilometerGridCell getCell() {
        return cell;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HouseholdSlot that = (HouseholdSlot) o;
        return shape.equals(that.shape) &&
                cell.equals(that.cell);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shape, cell);
    }
}
