package pl.edu.icm.board.agesex;

import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.household.model.AgeRange;

public enum AgeSex {

    M_0_4(AgeRange.AGE_0_4, Person.Sex.M), K_0_4(AgeRange.AGE_0_4, Person.Sex.K),
    M_5_9(AgeRange.AGE_5_9, Person.Sex.M), K_5_9(AgeRange.AGE_5_9, Person.Sex.K),
    M_10_14(AgeRange.AGE_10_14, Person.Sex.M), K_10_14(AgeRange.AGE_10_14, Person.Sex.K),
    M_15_19(AgeRange.AGE_15_19, Person.Sex.M), K_15_19(AgeRange.AGE_15_19, Person.Sex.K),
    M_20_24(AgeRange.AGE_20_24, Person.Sex.M), K_20_24(AgeRange.AGE_20_24, Person.Sex.K),
    M_25_29(AgeRange.AGE_25_29, Person.Sex.M), K_25_29(AgeRange.AGE_25_29, Person.Sex.K),
    M_30_34(AgeRange.AGE_30_34, Person.Sex.M), K_30_34(AgeRange.AGE_30_34, Person.Sex.K),
    M_35_39(AgeRange.AGE_35_39, Person.Sex.M), K_35_39(AgeRange.AGE_35_39, Person.Sex.K),
    M_40_44(AgeRange.AGE_40_44, Person.Sex.M), K_40_44(AgeRange.AGE_40_44, Person.Sex.K),
    M_45_49(AgeRange.AGE_45_49, Person.Sex.M), K_45_49(AgeRange.AGE_45_49, Person.Sex.K),
    M_50_54(AgeRange.AGE_50_54, Person.Sex.M), K_50_54(AgeRange.AGE_50_54, Person.Sex.K),
    M_55_59(AgeRange.AGE_55_59, Person.Sex.M), K_55_59(AgeRange.AGE_55_59, Person.Sex.K),
    M_60_64(AgeRange.AGE_60_64, Person.Sex.M), K_60_64(AgeRange.AGE_60_64, Person.Sex.K),
    M_65_69(AgeRange.AGE_65_69, Person.Sex.M), K_65_69(AgeRange.AGE_65_69, Person.Sex.K),
    M_70_74(AgeRange.AGE_70_74, Person.Sex.M), K_70_74(AgeRange.AGE_70_74, Person.Sex.K),
    M_75_79(AgeRange.AGE_75_79, Person.Sex.M), K_75_79(AgeRange.AGE_75_79, Person.Sex.K),
    M_80_(AgeRange.AGE_80_, Person.Sex.M), K_80_(AgeRange.AGE_80_, Person.Sex.K);

    private final static AgeSex[] values = values();

    private final Person.Sex sex;
    private final AgeRange ageRange;

    AgeSex(AgeRange ageRange, Person.Sex sex) {
        this.ageRange = ageRange;
        this.sex = sex;
    }

    public static AgeSex fromAgeSex(int age, Person.Sex sex) {
        var ageRangeFromAge = AgeRange.fromAge(age);
        for (var value : values) {
            if (value.getSex().equals(sex) && value.getAgeRange().equals(ageRangeFromAge)) {
                return value;
            }
        }
        throw new IllegalArgumentException("no such AgeSex value: " + age + " " + sex);
    }

    public static AgeSex fromAgeRangeSex(AgeRange ageRange, Person.Sex sex) {
        for (var value : values) {
            if (value.getSex().equals(sex) && value.getAgeRange().equals(ageRange)) {
                return value;
            }
        }
        throw new IllegalArgumentException("no such AgeSex value: " + ageRange + " " + sex);
    }

    public Person.Sex getSex() {
        return sex;
    }

    public AgeRange getAgeRange() {
        return ageRange;
    }
}
