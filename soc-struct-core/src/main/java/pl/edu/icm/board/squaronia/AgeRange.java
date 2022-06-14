package pl.edu.icm.board.squaronia;


public enum AgeRange {
    AGE_0_4(0,5),
    AGE_5_9(5,10),
    AGE_10_14(10,15),
    AGE_15_19(15,20),
    AGE_20_24(20,25),
    AGE_25_29(25,30),
    AGE_30_34(30,35),
    AGE_35_39(35,40),
    AGE_40_44(40,45),
    AGE_45_49(45,50),
    AGE_50_54(50,55),
    AGE_55_59(55,60),
    AGE_60_64(60,65),
    AGE_65_69(65,70),
    AGE_70_74(70,75),
    AGE_75_79(75,80),
    AGE_80_(80,100),
    AGE_0_100(0,100);

    private final int ageFrom;
    private final int ageTo;

    AgeRange(int ageFrom, int ageTo) {
        this.ageFrom = ageFrom;
        this.ageTo = ageTo;
    }

    int getValue(double random) {
        return (int)(ageFrom + (ageTo - ageFrom) * random);
    }

    @Override
    public String toString() {
        return "AgeRange{" +
                + ageFrom +
                "-" + ageTo +
                '}';
    }
}

