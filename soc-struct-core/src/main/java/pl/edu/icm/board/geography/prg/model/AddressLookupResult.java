package pl.edu.icm.board.geography.prg.model;

import pl.edu.icm.board.model.Location;

public class AddressLookupResult {
    private AddressPoint addressPoint;
    private Location location;
    private LookupPrecision precision;

    public AddressPoint getAddressPoint() {
        return addressPoint;
    }

    public void setAddressPoint(AddressPoint addressPoint) {
        this.addressPoint = addressPoint;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public LookupPrecision getPrecision() {
        return precision;
    }

    public void setPrecision(LookupPrecision precision) {
        this.precision = precision;
    }
}
