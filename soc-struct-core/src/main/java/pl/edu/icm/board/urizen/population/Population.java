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

package pl.edu.icm.board.urizen.population;

import com.univocity.parsers.common.record.Record;
import pl.edu.icm.board.urizen.household.model.AgeRange;
import pl.edu.icm.em.socstruct.component.Person;
import pl.edu.icm.trurl.bin.Histogram;

public class Population {
    final private String teryt;

    final private Histogram<AgeRange> peopleByAge = new Histogram<>();
    final private Histogram<Person.Sex> peopleBySex = new Histogram<>();
    final private Histogram<AgeRange> people80andMore;
    final private Histogram<AgeRange> people75to80;
    final private Histogram<AgeRange> people70to75;
    final private Histogram<AgeRange> people65to70;
    final private Histogram<AgeRange> people60to65;
    final private Histogram<AgeRange> people0to60;
    final private Histogram<AgeRange> people20to60;
    final private Histogram<AgeRange> people20to100;
    private final Histogram<AgeRange> people20to24;

    public Population(Record record) {
        this.teryt = record.getString("Powiat");

        peopleBySex.add(Person.Sex.M, record.getInt("M_Ludnosc"));
        peopleBySex.add(Person.Sex.F, record.getInt("K_Ludnosc"));
        peopleByAge.add(AgeRange.AGE_0_4, record.getInt("O_Ludnosc_0_4"));
        peopleByAge.add(AgeRange.AGE_5_9, record.getInt("O_Ludnosc_5_9"));
        peopleByAge.add(AgeRange.AGE_10_14, record.getInt("O_Ludnosc_10_14"));
        peopleByAge.add(AgeRange.AGE_15_19, record.getInt("O_Ludnosc_15_19"));
        peopleByAge.add(AgeRange.AGE_20_24, record.getInt("O_Ludnosc_20_24"));
        peopleByAge.add(AgeRange.AGE_25_29, record.getInt("O_Ludnosc_25_29"));
        peopleByAge.add(AgeRange.AGE_30_34, record.getInt("O_Ludnosc_30_34"));
        peopleByAge.add(AgeRange.AGE_35_39, record.getInt("O_Ludnosc_35_39"));
        peopleByAge.add(AgeRange.AGE_40_44, record.getInt("O_Ludnosc_40_44"));
        peopleByAge.add(AgeRange.AGE_45_49, record.getInt("O_Ludnosc_45_49"));
        peopleByAge.add(AgeRange.AGE_50_54, record.getInt("O_Ludnosc_50_54"));
        peopleByAge.add(AgeRange.AGE_55_59, record.getInt("O_Ludnosc_55_59"));
        peopleByAge.add(AgeRange.AGE_60_64, record.getInt("O_Ludnosc_60_64"));
        peopleByAge.add(AgeRange.AGE_65_69, record.getInt("O_Ludnosc_65_69"));
        peopleByAge.add(AgeRange.AGE_70_74, record.getInt("O_Ludnosc_70_74"));
        peopleByAge.add(AgeRange.AGE_75_79, record.getInt("O_Ludnosc_75_79"));
        peopleByAge.add(AgeRange.AGE_80_, record.getInt("O_Ludnosc_80_i_wiecej"));

        people20to60 = peopleByAge.createSubPool(
                AgeRange.AGE_20_24,
                AgeRange.AGE_25_29,
                AgeRange.AGE_30_34,
                AgeRange.AGE_35_39,
                AgeRange.AGE_40_44,
                AgeRange.AGE_45_49,
                AgeRange.AGE_50_54,
                AgeRange.AGE_55_59
        );
        people20to100 = peopleByAge.createSubPool(
                AgeRange.AGE_20_24,
                AgeRange.AGE_25_29,
                AgeRange.AGE_30_34,
                AgeRange.AGE_35_39,
                AgeRange.AGE_40_44,
                AgeRange.AGE_45_49,
                AgeRange.AGE_50_54,
                AgeRange.AGE_55_59,
                AgeRange.AGE_60_64,
                AgeRange.AGE_65_69,
                AgeRange.AGE_70_74,
                AgeRange.AGE_75_79,
                AgeRange.AGE_80_
        );
        people0to60 = peopleByAge.createSubPool(
                AgeRange.AGE_0_4,
                AgeRange.AGE_5_9,
                AgeRange.AGE_10_14,
                AgeRange.AGE_15_19,
                AgeRange.AGE_20_24,
                AgeRange.AGE_25_29,
                AgeRange.AGE_30_34,
                AgeRange.AGE_35_39,
                AgeRange.AGE_40_44,
                AgeRange.AGE_45_49,
                AgeRange.AGE_50_54,
                AgeRange.AGE_55_59
        );
        people20to24 = peopleByAge.createSubPool(AgeRange.AGE_20_24);
        people60to65 = peopleByAge.createSubPool(AgeRange.AGE_60_64);
        people65to70 = peopleByAge.createSubPool(AgeRange.AGE_65_69);
        people70to75 = peopleByAge.createSubPool(AgeRange.AGE_70_74);
        people75to80 = peopleByAge.createSubPool(AgeRange.AGE_75_79);
        people80andMore = peopleByAge.createSubPool(AgeRange.AGE_80_);
    }

    public String getTeryt() {
        return teryt;
    }

    public Histogram<AgeRange> getPeopleByAge() {
        return peopleByAge;
    }

    public Histogram<Person.Sex> getPeopleBySex() {
        return peopleBySex;
    }

    public Histogram<AgeRange> getPeople80andMore() {
        return people80andMore;
    }

    public Histogram<AgeRange> getPeople75to80() {
        return people75to80;
    }

    public Histogram<AgeRange> getPeople70to75() {
        return people70to75;
    }

    public Histogram<AgeRange> getPeople65to70() {
        return people65to70;
    }

    public Histogram<AgeRange> getPeople60to65() {
        return people60to65;
    }

    public Histogram<AgeRange> getPeople0to60() {
        return people0to60;
    }

    public Histogram<AgeRange> getPeople20to60() {
        return people20to60;
    }

    public Histogram<AgeRange> getPeople20to24() {
        return people20to24;
    }

    public Histogram<AgeRange> getPeople20to100() {
        return people20to100;
    }
}
