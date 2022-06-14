package pl.edu.icm.board.urizen.household.model;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import pl.edu.icm.board.geography.prg.model.AddressPoint;
import pl.edu.icm.board.model.Location;

import java.util.List;

public class ComplexBlueprint {
    private final Location location;
    private final List<Integer> householdsId = new IntArrayList();

    private ComplexBlueprint(float easting, float northing) {
        this.location = Location.fromPl1992MeterCoords(easting, northing);
    }

    public Location getLocation() {
        return location;
    }

    public List<Integer> getHouseholdsId() {
        return householdsId;
    }

    public static ComplexBlueprint from(AddressPoint ap) {
        return new ComplexBlueprint(ap.getEasting(), ap.getNorthing());
    }

    public void addHouseholdId(int householdId) {
        householdsId.add(householdId);
    }

    public int getSize() {
        return householdsId.size();
    }
}
