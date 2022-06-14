package pl.edu.icm.board.geography.prg.model;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class IndexedAddressPoint {
    private String normalized;
    private String normalizedStreetName;
    private AddressPoint addressPoint;

    public IndexedAddressPoint() {
    }

    public IndexedAddressPoint(String normalized, String normalizedStreetName, AddressPoint addressPoint) {
        this.normalized = normalized;
        this.normalizedStreetName = normalizedStreetName;
        this.addressPoint = addressPoint;
    }

    public void setNormalized(String normalized) {
        this.normalized = normalized;
    }

    public void setNormalizedStreetName(String normalizedStreetName) {
        this.normalizedStreetName = normalizedStreetName;
    }

    public void setAddressPoint(AddressPoint addressPoint) {
        this.addressPoint = addressPoint;
    }

    public String getNormalized() {
        return normalized;
    }

    public String getNormalizedStreetName() {
        return normalizedStreetName;
    }

    public AddressPoint getAddressPoint() {
        return addressPoint;
    }
}
