package pl.edu.icm.board.urizen.replicants;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.*;
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
        return createPerson(session, sex, age, ReplicantType.IMMIGRANTS_SPOT);
    }

    public Entity immigrantsSpotRoom(Session session, KilometerGridCell cell) {
        return createHousehold(session, cell, ReplicantType.IMMIGRANTS_SPOT);
    }

    public Entity nursingHomeResident(Session session, Person.Sex sex, int age) {
        return createPerson(session, sex, age, ReplicantType.NURSING_HOME);
    }

    public Entity nursingHomeRoom(Session session, List<Entity> complexHouseholds, KilometerGridCell cell) {
        return createHouseholdInComplex(session, cell, complexHouseholds, ReplicantType.NURSING_HOME);
    }

    public Entity dormResident(Session session, Person.Sex sex, int age) {
        return createPerson(session, sex, age, ReplicantType.DORM);
    }

    public Entity dormRoom(Session session, List<Entity> complexHouseholds, KilometerGridCell cell) {
        return createHouseholdInComplex(session, cell, complexHouseholds, ReplicantType.DORM);
    }

    public Entity monasteryResident(Session session, Person.Sex sex, int age) {
        return createPerson(session, sex, age, ReplicantType.MONASTERY);
    }

    public Entity monasteryRoom(Session session, List<Entity> complexHouseholds, KilometerGridCell cell) {
        return createHouseholdInComplex(session, cell, complexHouseholds, ReplicantType.MONASTERY);
    }

    public Entity prisonResident(Session session, Person.Sex sex, int age) {
        return createPerson(session, sex, age, ReplicantType.PRISON);
    }

    public Entity prisonRoom(Session session, List<Entity> complexHouseholds, Location location) {
        return createHouseholdInComplex(session, location, complexHouseholds, ReplicantType.PRISON);
    }

    public Entity barracksRoom(Session session, List<Entity> complexHouseholds, KilometerGridCell cell) {
        return createHouseholdInComplex(session, cell, complexHouseholds, ReplicantType.BARRACKS);
    }

    public Entity barracksResident(Session session, Person.Sex sex, int age) {
        return createPerson(session, sex, age, ReplicantType.BARRACKS);
    }

    public Entity clergyHouseResident(Session session, Person.Sex sex, int age) {
        return createPerson(session, sex, age, ReplicantType.CLERGY_HOUSE);
    }

    public Entity clergyHouseRoom(Session session, KilometerGridCell cell) {
        return createHousehold(session, cell, ReplicantType.CLERGY_HOUSE);
    }

    public Entity homelessSpotRoom(Session session, KilometerGridCell cell) {
        return createHousehold(session, cell, ReplicantType.HOMELESS_SPOT);
    }

    public Entity homelessSpotResident(Session session, Person.Sex sex, int age) {
        return createPerson(session, sex, age, ReplicantType.HOMELESS_SPOT);
    }

    private Entity createPerson(Session session, Person.Sex sex, int age, ReplicantType type) {
        Entity replicantEntity = session.createEntity();
        Person person = replicantEntity.add(new Person());
        person.setSex(sex);
        person.setAge(age);
        replicantEntity.add(replicant(type));
        return replicantEntity;
    }

    private Entity createHousehold(Session session, KilometerGridCell cell, ReplicantType type) {
        Entity roomEntity = session.createEntity();
        roomEntity.add(cell.toLocation());
        roomEntity.add(replicant(type));
        roomEntity.add(new Household());
        return roomEntity;
    }

    private Entity createHouseholdInComplex(Session session,
                                            KilometerGridCell cell,
                                            List<Entity> complexHouseholds,
                                            ReplicantType type) {
        return createHouseholdInComplex(session, cell.toLocation(), complexHouseholds, type);
    }

    private Entity createHouseholdInComplex(Session session,
                                            Location location,
                                            List<Entity> complexHouseholds,
                                            ReplicantType type) {
        Entity roomEntity = session.createEntity();
        roomEntity.add(location);
        roomEntity.add(replicant(type));
        roomEntity.add(new Household());
        complexHouseholds.add(roomEntity);
        return roomEntity;
    }

    private Replicant replicant(ReplicantType type) {
        Replicant replicant = new Replicant();
        replicant.setType(type);
        return replicant;
    }
}
