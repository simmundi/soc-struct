/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.board.urizen.household.cloner;

import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.agesex.AgeSexFromDistributionPicker;
import pl.edu.icm.em.common.detached.DetachedEntity;
import pl.edu.icm.em.socstruct.component.geo.AdministrationUnitTag;
import pl.edu.icm.em.socstruct.component.Household;
import pl.edu.icm.em.socstruct.component.Person;
import pl.edu.icm.board.urizen.household.model.AgeRange;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;

import java.util.Arrays;
import java.util.Objects;

public class HouseholdShape {
    private final int[] ageHistogram = new int[AgeRange.values().length];
    private final String teryt;
    private final AgeSexFromDistributionPicker ageSexFromDistributionPicker;

    private HouseholdShape(Household household, AdministrationUnitTag administrationUnitTag, AgeSexFromDistributionPicker ageSexFromDistributionPicker) {
        this.ageSexFromDistributionPicker = ageSexFromDistributionPicker;
        for (Entity member : household.getMembers()) {
            Person person = member.get(Person.class);
            ageHistogram[AgeRange.fromAge(person.getAge()).ordinal()]++;
        }
        this.teryt = administrationUnitTag.getCode();
    }

    public static HouseholdShape tryCreate(DetachedEntity householdEntity, AgeSexFromDistributionPicker ageSexFromDistributionPicker) {
        Household household = householdEntity.get(Household.class);
        AdministrationUnitTag administrationUnitTag = householdEntity.get(AdministrationUnitTag.class);
        if (household != null && administrationUnitTag != null) {
            return new HouseholdShape(household, administrationUnitTag, ageSexFromDistributionPicker);
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
        householdEntity.add(new AdministrationUnitTag(teryt));
        var members = householdEntity.add(new Household()).getMembers();

        for (int i = 0; i < ageHistogram.length; i++) {
            for (int j = 0; j < ageHistogram[i]; j++) {
                Entity memberEntity = session.createEntity();
                members.add(memberEntity);

                Person person = memberEntity.add(new Person());
                var ageRange = AgeRange.fromOrdinal(ageHistogram[i]);
                var sex = randomGenerator.nextBoolean() ? Person.Sex.M : Person.Sex.F;
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
