package pl.edu.icm.board.urizen.replicants;

import net.snowyhollows.bento.annotation.WithFactory;
import org.apache.commons.math3.random.RandomDataGenerator;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.agesex.AgeSexFromDistributionPicker;
import pl.edu.icm.board.model.Complex;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.geography.density.PopulationDensityLoader;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Replicant;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.board.urizen.household.model.AgeRange;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.bin.BinPool;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;

import java.io.FileNotFoundException;
import java.util.List;

public class BarracksUrizen {
    private final Board board;
    private final PopulationDensityLoader populationDensityLoader;
    private final ReplicantPrototypes prototypes;
    private final RandomDataGenerator random;
    private final int barracksReplicantsCount;
    private final int barracksRoomSize;
    private final int barracksMaxRooms;
    private final BinPool<AgeRange> ages;
    private final BinPool<Person.Sex> sexes;
    private final AgeSexFromDistributionPicker ageSexFromDistributionPicker;
    private final Entities entities;

    @WithFactory
    public BarracksUrizen(
            Board board,
            ReplicantPrototypes prototypes,
            Entities entities, ReplicantsPopulation replicantsPopulation,
            PopulationDensityLoader populationDensityLoader,
            AgeSexFromDistributionPicker ageSexFromDistributionPicker, RandomProvider randomProvider,
            int barracksReplicantsCount, int barracksRoomSize, int barracksMaxRooms) {
        this.board = board;
        this.prototypes = prototypes;
        this.entities = entities;
        this.populationDensityLoader = populationDensityLoader;
        this.ageSexFromDistributionPicker = ageSexFromDistributionPicker;
        this.barracksReplicantsCount = barracksReplicantsCount;
        this.barracksRoomSize = barracksRoomSize;
        this.barracksMaxRooms = barracksMaxRooms;
        this.board.require(Household.class, Person.class, Location.class, Replicant.class);
        this.sexes = replicantsPopulation.getPopulation().getPeopleBySex();
        this.ages = replicantsPopulation.getPopulation().getPeopleByAge().createSubPool(
                AgeRange.AGE_20_24,
                AgeRange.AGE_25_29,
                AgeRange.AGE_30_34,
                AgeRange.AGE_35_39,
                AgeRange.AGE_40_44,
                AgeRange.AGE_45_49
        );
        this.random = randomProvider.getRandomDataGenerator(BarracksUrizen.class);
    }

    public void fabricate() throws FileNotFoundException {
        populationDensityLoader.load();
        int maxSize = barracksMaxRooms * barracksRoomSize;
        int minSize = barracksRoomSize;
        int count = barracksReplicantsCount;
        while (count > 0) {
            int size = Math.min(random.nextInt(minSize, maxSize), count);
            generateBarracks(size);
            count -= size;
        }
    }

    private void generateBarracks(int barracksSize) {
        board.getEngine().execute(sessionFactory -> {
            Session session = sessionFactory.create();
            Entity entity = entities.createEmptyComplex(session, barracksSize);
            Complex complex = entity.get(Complex.class);
            complex.setType(Complex.Type.BARRACKS);
            KilometerGridCell cell = populationDensityLoader
                    .sample(random.getRandomGenerator().nextDouble());
            entity.add(cell.toLocation());
            List<Entity> complexHouseholds = complex.getHouseholds();
            int leftToCreate = barracksSize;
            while (leftToCreate > 0) {
                int roomSize = Math.min(barracksRoomSize, leftToCreate);
                generateRoom(roomSize, complexHouseholds, session, cell);
                leftToCreate -= roomSize;
            }
        });
    }

    private void generateRoom(int inhabitants, List<Entity> complexHouseholds, Session session, KilometerGridCell cell) {
            List<Entity> dependents = prototypes.barracksRoom(session, complexHouseholds, cell).get(Household.class).getMembers();
            for (int i = 0; i < inhabitants; i++) {
                var sexPicked = sexes.sample(random.getRandomGenerator().nextDouble()).pick();
                var ageRangePicked = ages.sample(random.getRandomGenerator().nextDouble()).pick();
                dependents.add(prototypes.barracksResident(session, sexPicked, ageSexFromDistributionPicker.getEmpiricalDistributedRandomAge(sexPicked, ageRangePicked, random.getRandomGenerator().nextDouble())));
            }
    }
}
