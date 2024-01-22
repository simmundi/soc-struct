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

package pl.edu.icm.board.urizen.replicants;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.*;
import pl.edu.icm.em.socstruct.component.Household;
import pl.edu.icm.em.socstruct.component.Person;
import pl.edu.icm.em.socstruct.component.geo.Location;
import pl.edu.icm.em.socstruct.component.prefab.PrefabTag;
import pl.edu.icm.em.socstruct.component.prefab.PrefabType;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;

import java.util.List;

/**
 * Creates prototypes for replicants
 */
public class ReplicantPrototypes {

    @WithFactory
    public ReplicantPrototypes() {
    }

    public Entity immigrantsSpotResident(Session session, Person.Sex sex, int age) {
        return createPerson(session, sex, age, PrefabType.IMMIGRANTS_SPOT);
    }

    public Entity immigrantsSpotRoom(Session session, KilometerGridCell cell) {
        return createHousehold(session, cell, PrefabType.IMMIGRANTS_SPOT);
    }

    public Entity nursingHomeResident(Session session, Person.Sex sex, int age) {
        return createPerson(session, sex, age, PrefabType.NURSING_HOME);
    }

    public Entity nursingHomeRoom(Session session, List<Entity> complexHouseholds, KilometerGridCell cell) {
        return createHouseholdInComplex(session, cell, complexHouseholds, PrefabType.NURSING_HOME);
    }

    public Entity dormResident(Session session, Person.Sex sex, int age) {
        return createPerson(session, sex, age, PrefabType.DORM);
    }

    public Entity dormRoom(Session session, List<Entity> complexHouseholds, KilometerGridCell cell) {
        return createHouseholdInComplex(session, cell, complexHouseholds, PrefabType.DORM);
    }

    public Entity monasteryResident(Session session, Person.Sex sex, int age) {
        return createPerson(session, sex, age, PrefabType.MONASTERY);
    }

    public Entity monasteryRoom(Session session, List<Entity> complexHouseholds, KilometerGridCell cell) {
        return createHouseholdInComplex(session, cell, complexHouseholds, PrefabType.MONASTERY);
    }

    public Entity prisonResident(Session session, Person.Sex sex, int age) {
        return createPerson(session, sex, age, PrefabType.PRISON);
    }

    public Entity prisonRoom(Session session, List<Entity> complexHouseholds, Location location) {
        return createHouseholdInComplex(session, location, complexHouseholds, PrefabType.PRISON);
    }

    public Entity barracksRoom(Session session, List<Entity> complexHouseholds, KilometerGridCell cell) {
        return createHouseholdInComplex(session, cell, complexHouseholds, PrefabType.BARRACKS);
    }

    public Entity barracksResident(Session session, Person.Sex sex, int age) {
        return createPerson(session, sex, age, PrefabType.BARRACKS);
    }

    public Entity clergyHouseResident(Session session, Person.Sex sex, int age) {
        return createPerson(session, sex, age, PrefabType.CLERGY_HOUSE);
    }

    public Entity clergyHouseRoom(Session session, KilometerGridCell cell) {
        return createHousehold(session, cell, PrefabType.CLERGY_HOUSE);
    }

    public Entity homelessSpotRoom(Session session, KilometerGridCell cell) {
        return createHousehold(session, cell, PrefabType.HOMELESS_SPOT);
    }

    public Entity homelessSpotResident(Session session, Person.Sex sex, int age) {
        return createPerson(session, sex, age, PrefabType.HOMELESS_SPOT);
    }

    private Entity createPerson(Session session, Person.Sex sex, int age, PrefabType type) {
        Entity replicantEntity = session.createEntity();
        Person person = replicantEntity.add(new Person());
        person.setSex(sex);
        person.setAge(age);
        replicantEntity.add(replicant(type));
        return replicantEntity;
    }

    private Entity createHousehold(Session session, KilometerGridCell cell, PrefabType type) {
        Entity roomEntity = session.createEntity();
        roomEntity.add(cell.toLocation());
        roomEntity.add(replicant(type));
        roomEntity.add(new Household());
        return roomEntity;
    }

    private Entity createHouseholdInComplex(Session session,
                                            KilometerGridCell cell,
                                            List<Entity> complexHouseholds,
                                            PrefabType type) {
        return createHouseholdInComplex(session, cell.toLocation(), complexHouseholds, type);
    }

    private Entity createHouseholdInComplex(Session session,
                                            Location location,
                                            List<Entity> complexHouseholds,
                                            PrefabType type) {
        Entity roomEntity = session.createEntity();
        roomEntity.add(location);
        roomEntity.add(replicant(type));
        roomEntity.add(new Household());
        complexHouseholds.add(roomEntity);
        return roomEntity;
    }

    private PrefabTag replicant(PrefabType type) {
        PrefabTag prefabTag = new PrefabTag();
        prefabTag.setType(type);
        return prefabTag;
    }
}
