package pl.edu.icm.board.urizen.household.cloner;

import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.agesex.AgeSexFromDistributionPicker;
import pl.edu.icm.board.model.AdministrationUnit;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.household.model.AgeRange;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;

import java.util.Arrays;
import java.util.Objects;

public class HouseholdShape {
    private final int[] ageHistogram = new int[AgeRange.values().length];
    private final String teryt;
    private final AgeSexFromDistributionPicker ageSexFromDistributionPicker;

    private HouseholdShape(Household household, AdministrationUnit administrationUnit, AgeSexFromDistributionPicker ageSexFromDistributionPicker) {
        this.ageSexFromDistributionPicker = ageSexFromDistributionPicker;
        for (Entity member : household.getMembers()) {
            Person person = member.get(Person.class);
            ageHistogram[AgeRange.fromAge(person.getAge()).ordinal()]++;
        }
        this.teryt = administrationUnit.getTeryt();
    }

    public static HouseholdShape tryCreate(Entity householdEntity, AgeSexFromDistributionPicker ageSexFromDistributionPicker) {
        Household household = householdEntity.get(Household.class);
        AdministrationUnit administrationUnit = householdEntity.get(AdministrationUnit.class);
        if (household != null && administrationUnit != null) {
            return new HouseholdShape(household, administrationUnit, ageSexFromDistributionPicker);
        } else {
            return null;
        }
    }

    public String getTeryt() {
        return teryt;
    }

    public int getMemberCount() {
        return Arrays.stream(ageHistogram).sum();
    }

    public void createHouse(Session session, RandomGenerator randomGenerator) {
        var householdEntity = session.createEntity();
        householdEntity.add(new AdministrationUnit(teryt));
        var members = householdEntity.add(new Household()).getMembers();

        for (int i = 0; i < ageHistogram.length; i++) {
            for (int j = 0; j < ageHistogram[i]; j++) {
                Entity memberEntity = session.createEntity();
                members.add(memberEntity);

                Person person = memberEntity.add(new Person());
                var ageRange = AgeRange.fromOrdinal(ageHistogram[i]);
                var sex = randomGenerator.nextBoolean() ? Person.Sex.M : Person.Sex.K;
                person.setAge(ageSexFromDistributionPicker
                        .getEmpiricalDistributedRandomAge(sex, ageRange, randomGenerator.nextDouble()));
                person.setSex(sex);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HouseholdShape that = (HouseholdShape) o;
        return Arrays.equals(ageHistogram, that.ageHistogram) &&
                Objects.equals(teryt, that.teryt);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(teryt);
        result = 31 * result + Arrays.hashCode(ageHistogram);
        return result;
    }
}
