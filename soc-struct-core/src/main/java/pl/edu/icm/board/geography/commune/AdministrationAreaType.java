package pl.edu.icm.board.geography.commune;

public enum AdministrationAreaType {
    VILLAGE,
    CITY_S,
    CITY_M,
    CITY_L,
    CITY_XL;

    public static AdministrationAreaType fromCityPopulation(int population) {
        if (population >= 100_000) {
            return CITY_XL;
        } else if (population >= 50_000) {
            return CITY_L;
        } else if (population >= 20_000) {
            return CITY_M;
        } else return CITY_S;
    }
}
