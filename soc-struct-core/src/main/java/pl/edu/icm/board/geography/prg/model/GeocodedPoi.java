package pl.edu.icm.board.geography.prg.model;

public class GeocodedPoi<T> {
    private final AddressLookupResult addressLookupResult;
    private final T poi;

    public GeocodedPoi(AddressLookupResult addressLookupResult, T poi) {
        this.addressLookupResult = addressLookupResult;
        this.poi = poi;
    }

    public AddressLookupResult getAddressLookupResult() {
        return addressLookupResult;
    }

    public T getPoi() {
        return poi;
    }
}
