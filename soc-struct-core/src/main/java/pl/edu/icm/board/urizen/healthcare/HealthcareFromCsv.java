package pl.edu.icm.board.urizen.healthcare;

import pl.edu.icm.board.model.HealthcareType;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

;

@WithMapper
public class HealthcareFromCsv {

    private HealthcareType type;
    private String communeTeryt;
    private String name;
    private String locality;
    private String postalCode;
    private String street;
    private String streetNumber;
    private String dateOfClosure;

    public HealthcareType getType() {
        return type;
    }

    public void setType(HealthcareType type) {
        this.type = type;
    }

    public void setType(String type) {
        if ("0010".equals(type)) {
            this.type = HealthcareType.POZ;
        } else {
            this.type = HealthcareType.OTHER;
        }
    }

    public String getCommuneTeryt() {
        return communeTeryt;
    }

    public void setCommuneTeryt(String communeTeryt) {
        this.communeTeryt = communeTeryt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getDateOfClosure() { return dateOfClosure; }

    public void setDateOfClosure(String dateOfClosure) { this.dateOfClosure = dateOfClosure; }
}
