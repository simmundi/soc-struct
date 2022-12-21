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
