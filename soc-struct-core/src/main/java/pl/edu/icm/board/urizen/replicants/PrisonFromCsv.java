package pl.edu.icm.board.urizen.replicants;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;;

@WithMapper
public class PrisonFromCsv {

    public enum Type {
        PRISON_M,
        PRISON_K,
        PRISON_MK;


        public static Type from(String type) {
            switch (type) {
                case "M":
                    return PRISON_M;
                case "K":
                    return PRISON_K;
                case "MK":
                    return PRISON_MK;
                default:
                    throw new IllegalArgumentException("Unknown prison type: " + type);
            }
        }
    }

    private Type type;
    private String communeTeryt;
    private String name;
    private String locality;
    private String postalCode;
    private String street;
    private String streetNumber;
    private int prisonCount;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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

    public int getPrisonCount() {
        return prisonCount;
    }

    public void setPrisonCount(int prisonCount) {
        this.prisonCount = prisonCount;
    }
}
