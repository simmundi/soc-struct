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

package pl.edu.icm.board.urizen.generic;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.model.*;
import pl.edu.icm.board.urizen.household.model.ComplexBlueprint;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;

public class Entities {

    @WithFactory
    public Entities() {
    }

    public Entity createCitizen(Session session, Person.Sex sex, int age) {
        Entity entity = session.createEntity();
        Person person = entity.add(new Person());
        person.setAge(age);
        person.setSex(sex);
        return entity;
    }

    public Entity createEmptyHousehold(Session session, String teryt) {
        Entity entity = session.createEntity();
        entity.add(new AdministrationUnit(teryt));
        entity.add(new Household());
        return entity;
    }

    public Entity createEmptyComplex(Session session, int size) {
        Entity entity = session.createEntity();
        entity.add(new Location());
        entity.add(new Complex(size));
        return entity;
    }

    public Entity createEducationInstitution(Session session, String name, Location location, EducationLevel level, int pupilCount, int teacherCount, String teryt) {
        Entity entity = session.createEntity();

        Named named = new Named();
        named.setName(name);

        AdministrationUnit administrationUnit = new AdministrationUnit();
        administrationUnit.setTeryt(teryt);

        EducationalInstitution educationalInstitution = new EducationalInstitution();
        educationalInstitution.setLevel(level);
        educationalInstitution.setPupilCount(pupilCount);
        educationalInstitution.setTeacherCount((short) teacherCount);

        entity.add(location);
        entity.add(named);
        entity.add(educationalInstitution);
        entity.add(administrationUnit);

        return entity;
    }

    public Entity createHealthcare(Session session, HealthcareType type, int northing, int easting) {
        Entity entity = session.createEntity();
        Location location = new Location();
        location.setN(northing);
        location.setE(easting);
        entity.add(location);
        Healthcare healthcare = new Healthcare();
        healthcare.setType(type);
        entity.add(healthcare);
        return entity;
    }

    public void createPatient(Entity patientEntity, Entity healthcareUnitEntity) {
        Patient patient = patientEntity.get(Patient.class);
        patient = patient != null ? patient : patientEntity.add(new Patient());
        patient.setHealthcare(healthcareUnitEntity);
    }

    public void attends(Entity attendeeEntity, Entity placeEntity) {
        Attendee attendee = attendeeEntity.get(Attendee.class);
        attendee = attendee != null ? attendee : attendeeEntity.add(new Attendee());
        attendee.setInstitution(placeEntity);
    }

    public void attendsAsSecondary(Entity attendeeEntity, Entity placeEntity) {
        Attendee attendee = attendeeEntity.get(Attendee.class);
        attendee = attendee != null ? attendee : attendeeEntity.add(new Attendee());
        attendee.setSecondaryInstitution(placeEntity);
    }

    public void createBuildingFromBlueprint(Session session, ComplexBlueprint complexBlueprint) {
        var complexEntity = session.createEntity();
        var complexLocation = complexBlueprint.getLocation();

        var complex = complexEntity.add(new Complex(complexBlueprint.getSize()));
        complexEntity.add(complexLocation);
        var complexHouseholds = complex.getHouseholds();
        complex.setType(Complex.Type.RESIDENTIAL_HOUSE);

        complexBlueprint.getHouseholdsId().forEach(id -> {
            var householdEntity = session.getEntity(id);
            householdEntity.add(complexLocation);
            complexHouseholds.add(householdEntity);
        });

    }
}
