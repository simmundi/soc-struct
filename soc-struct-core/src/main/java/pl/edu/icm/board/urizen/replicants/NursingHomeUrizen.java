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

/**
 * This should be corrected using:
 * https://stat.gov.pl/files/gfx/portalinformacyjny/pl/defaultaktualnosci/5487/18/3/1/zaklady_stacjonarne_pomocy_spolecznej_w_2018.pdf
 */
public class NursingHomeUrizen {
    private final Board board;
    private final PopulationDensityLoader populationDensityLoader;
    private final ReplicantPrototypes prototypes;
    private final RandomDataGenerator random;
    private final int nursingHomeReplicantsCount;
    private final int nursingHomeRoomSize;
    private final int nursingHomeMaxRooms;
    private final BinPool<AgeRange> ages;
    private final BinPool<Person.Sex> sexes;
    private final AgeSexFromDistributionPicker ageSexFromDistributionPicker;
    private Entities entities;

    @WithFactory
    public NursingHomeUrizen(
            Board board,
            ReplicantPrototypes prototypes,
            Entities entities, ReplicantsPopulation replicantsPopulation,
            PopulationDensityLoader populationDensityLoader,
            AgeSexFromDistributionPicker ageSexFromDistributionPicker, RandomProvider randomProvider,
            int nursingHomeReplicantsCount, int nursingHomeRoomSize, int nursingHomeMaxRooms) {
        this.board = board;
        this.prototypes = prototypes;
        this.entities = entities;
        this.populationDensityLoader = populationDensityLoader;
        this.ageSexFromDistributionPicker = ageSexFromDistributionPicker;
        this.nursingHomeReplicantsCount = nursingHomeReplicantsCount;
        this.nursingHomeRoomSize = nursingHomeRoomSize;
        this.nursingHomeMaxRooms = nursingHomeMaxRooms;
        this.board.require(Household.class, Person.class, Location.class, Replicant.class);
        this.sexes = replicantsPopulation.getPopulation().getPeopleBySex();
        this.ages = replicantsPopulation.getPopulation().getPeopleByAge().createSubPool(
                AgeRange.AGE_70_74,
                AgeRange.AGE_75_79,
                AgeRange.AGE_80_
        );
        this.random = randomProvider.getRandomDataGenerator(NursingHomeUrizen.class);
    }

    public void fabricate() throws FileNotFoundException {
        populationDensityLoader.load();
        int maxSize = nursingHomeMaxRooms * nursingHomeRoomSize;
        int minSize = nursingHomeRoomSize;
        int count = nursingHomeReplicantsCount;
        while (count > 0) {
            int size = Math.min(random.nextInt(minSize, maxSize), count);
            generateNursingHome(size);
            count -= size;
        }
    }

    private void generateNursingHome(int nursingHomeSize) {
        board.getEngine().execute(sessionFactory -> {
            Session session = sessionFactory.create();
            Entity entity = entities.createEmptyComplex(session, nursingHomeSize);
            Complex complex = entity.get(Complex.class);
            complex.setType(Complex.Type.NURSING_HOME);
            KilometerGridCell cell = populationDensityLoader.sample(random.getRandomGenerator().nextDouble());
            entity.add(cell.toLocation());
            List<Entity> complexHouseholds = complex.getHouseholds();
            int leftToCreate = nursingHomeSize;

            while (leftToCreate > 0) {
                int roomSize = Math.min(nursingHomeRoomSize, leftToCreate);
                generateRoom(roomSize, complexHouseholds, session, cell);
                leftToCreate -= roomSize;
            }
            session.close();
        });
    }

    private void generateRoom(int inhabitants, List<Entity> complexHouseholds, Session session, KilometerGridCell cell) {
            List<Entity> dependents = prototypes.nursingHomeRoom(session, complexHouseholds, cell).get(Household.class).getMembers();
            for (int i = 0; i < inhabitants; i++) {
                var sexPicked = sexes.sample(random.getRandomGenerator().nextDouble()).pick();
                var ageRangePicked = ages.sample(random.getRandomGenerator().nextDouble()).pick();
                dependents.add(prototypes.nursingHomeResident(session, sexPicked, ageSexFromDistributionPicker.getEmpiricalDistributedRandomAge(sexPicked, ageRangePicked, random.getRandomGenerator().nextDouble())));
            }
    }

}
