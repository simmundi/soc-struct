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

package pl.edu.icm.board.urizen.place;

import pl.edu.icm.em.socstruct.component.edu.EducationLevel;
import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

import java.util.regex.Pattern;

@WithDao
public class EducationalInstitutionFromCsv {

    private static Pattern PRESCHOOL_PATTERN = Pattern.compile("przedszkol", Pattern.CASE_INSENSITIVE);
    private static Pattern PRIMARY_PATTERN = Pattern.compile("\\Wi stopnia|podstawowa|poznańska szkoła chóralna", Pattern.CASE_INSENSITIVE);
    private static Pattern HIGH_PATTERN = Pattern.compile("\\Wii stopnia|technikum|liceum|szkoła sztuki|ogólnokształcąc(?!.*\\Wi stopnia)|szkoła realna", Pattern.CASE_INSENSITIVE);
    private static Pattern BOTH_PATTERN = Pattern.compile("inna szkoła artystyczna|młodzieżowy ośrodek|specjalny ośrodek|szkoła specjalna przysposabiająca|zakład poprawczy|zespół szkół", Pattern.CASE_INSENSITIVE);
    private static Pattern ADULTS_PATTERN = Pattern.compile("kolegium|ustawiczne|policealn[ae]", Pattern.CASE_INSENSITIVE);

    public enum Level {
        PRESCHOOL,
        PRIMARY,
        HIGH,
        ADULTS,
        PRIMARY_AND_HIGH,
        UNKNOWN;

        public static Level from(String typeName) {
            if (PRESCHOOL_PATTERN.matcher(typeName).find()) {
                return PRESCHOOL;
            } else if (PRIMARY_PATTERN.matcher(typeName).find()) {
                return PRIMARY;
            } else if (HIGH_PATTERN.matcher(typeName).find()) {
                return HIGH;
            } else if (ADULTS_PATTERN.matcher(typeName).find()) {
                return ADULTS;
            } else if (BOTH_PATTERN.matcher(typeName).find()) {
                return PRIMARY_AND_HIGH;
            }
            return UNKNOWN;
        }
    }

    private Level level;
    private String communeTeryt;
    private String name;
    private String locality;
    private String postalCode;
    private String street;
    private String streetNumber;
    private int pupils;
    private int teachers;

    public String getCommuneTeryt() {
        return communeTeryt;
    }

    public void setCommuneTeryt(String teryt) {
        this.communeTeryt = teryt;
    }

    public int getPupils() {
        return pupils;
    }

    public void setPupils(int pupils) {
        this.pupils = pupils;
    }

    public int getTeachers() {
        return teachers;
    }

    public void setTeachers(int teachers) {
        this.teachers = teachers;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
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

    public static EducationLevel fromLevel(EducationalInstitutionFromCsv.Level level) {
        if (level == null) {
            return null;
        }
        switch (level) {
            case PRESCHOOL:
                return EducationLevel.K;
            case PRIMARY:
                return EducationLevel.P;
            case HIGH:
                return EducationLevel.H;
            case PRIMARY_AND_HIGH:
                return EducationLevel.PH;
            case ADULTS:
            case UNKNOWN:
            default:
                return null;
        }
    }
}
