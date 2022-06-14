package pl.edu.icm.board.geography.prg.model;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class AddressPoint {
    private String prgId;
    private String postalCode;
    private String locality;
    private String fineLocality;
    private String street;
    private String number;
    private float easting;
    private float northing;

    public String getPrgId() {
        return prgId;
    }

    public void setPrgId(String prgId) {
        this.prgId = prgId;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getFineLocality() {
        return fineLocality;
    }

    public void setFineLocality(String fineLocality) {
        this.fineLocality = fineLocality;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public float getEasting() {
        return easting;
    }

    public void setEasting(float easting) {
        this.easting = easting;
    }

    public float getNorthing() {
        return northing;
    }

    public void setNorthing(float northing) {
        this.northing = northing;
    }
}
