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

package pl.edu.icm.board.urizen.household.model;

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
    AGE_80_(80,101);

    private final static AgeRange[] values = values();

    private final int ageFrom;
    private final int ageTo;

    AgeRange(int ageFrom, int ageTo) {
        this.ageFrom = ageFrom;
        this.ageTo = ageTo;
    }

    public static AgeRange fromOrdinal(int ordinal) {
        return values[ordinal];
    }

    public static AgeRange fromAge(int age) {
        for (int i = 0; i < values.length; i++) {
            if (age < values[i].ageTo) {
                return values[i];
            }
        }
        throw new IllegalArgumentException("age outside the range: " + age);
    }

    @Override
    public String toString() {
        return "AgeRange{" +
                ageFrom +
                "-" + ageTo +
                '}';
    }
}
