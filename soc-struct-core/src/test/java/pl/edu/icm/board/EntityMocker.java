package pl.edu.icm.board;

import pl.edu.icm.board.model.AdministrationUnit;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.MapperSet;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.util.DynamicComponentAccessor;

import java.util.HashMap;
import java.util.Map;

public class EntityMocker {

    private final MapperSet mapperSet;
    private final Session session;
    private Map<Integer, Entity> entities = new HashMap<>();

    public EntityMocker(Session session, Class<?>... componentClasses) {

        this.mapperSet = new MapperSet(new DynamicComponentAccessor(componentClasses));
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

    public AdministrationUnit au(String teryt) {
        return new AdministrationUnit(teryt);
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
