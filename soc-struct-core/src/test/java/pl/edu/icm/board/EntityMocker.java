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

package pl.edu.icm.board;

import net.snowyhollows.bento.Bento;
import pl.edu.icm.em.socstruct.component.geo.AdministrationUnitTag;
import pl.edu.icm.em.socstruct.component.Household;
import pl.edu.icm.em.socstruct.component.Person;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.MapperSet;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.ecs.util.DynamicComponentAccessor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EntityMocker {

    private final Bento bento = Bento.createRoot();
    private final MapperSet mapperSet;
    private final Session session;
    private Map<Integer, Entity> entities = new HashMap<>();

    public EntityMocker(Session session, Class<?>... componentClasses) {

        this.mapperSet = new MapperSet(new DynamicComponentAccessor(Arrays.asList(componentClasses)), new Mappers(bento));
        this.session = session;
    }

    public Entity id(int id) {
        return entities.get(id);
    }

    public Household household(int... ids) {
        Household household = new Household();
        for (int id : ids) {
            household.getMembers().add(id(id));
        }
        return household;
    }

    public Person person(int age, Person.Sex sex) {
        Person person = new Person();
        person.setAge(age);
        person.setSex(sex);
        return person;
    }

    public AdministrationUnitTag au(String teryt) {
        return new AdministrationUnitTag(teryt);
    }

    public Entity entity(int id, Object... components) {
        Entity entity = new Entity(mapperSet, session, id);
        entities.put(id, entity);
        for (Object component : components) {
            entity.add(component);
        }
        return entity;
    }


}
