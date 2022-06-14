package pl.edu.icm.board.urizen.replicants;

import net.snowyhollows.bento2.annotation.WithFactory;
import org.apache.commons.math3.random.RandomDataGenerator;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.agesex.AgeSexFromDistributionPicker;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.geography.density.PopulationDensityLoader;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Replicant;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.household.model.AgeRange;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.bin.Bin;
import pl.edu.icm.trurl.bin.BinPool;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;

import java.io.FileNotFoundException;
import java.util.List;

public class HomelessSpotUrizen {
    private final Board board;
    private final PopulationDensityLoader populationDensityLoader;
    private final ReplicantPrototypes prototypes;
    private final RandomDataGenerator random;

    private final int homelessSpotReplicantsCount;
    private final int homelessSpotRoomSize;
    private final int homelessSpotMaxInSingleGridCell;
    private final int homelessSpotPercentOfMale;
    private final BinPool<AgeRange> ages;
    private final BinPool<Person.Sex> sexSubPoolM;
    private final BinPool<Person.Sex> sexSubPoolK;
    private final AgeSexFromDistributionPicker ageSexFromDistributionPicker;

    @WithFactory
    public HomelessSpotUrizen(
            Board board,
            ReplicantPrototypes prototypes,
            ReplicantsPopulation replicantsPopulation,
            PopulationDensityLoader populationDensityLoader,
            AgeSexFromDistributionPicker ageSexFromDistributionPicker, RandomProvider randomProvider,
            int homelessSpotReplicantsCount, int homelessSpotRoomSize, int homelessSpotMaxInSingleGridCell, int homelessSpotPercentOfMale) {
        this.board = board;
        this.prototypes = prototypes;
        this.populationDensityLoader = populationDensityLoader;
        this.ageSexFromDistributionPicker = ageSexFromDistributionPicker;
        this.homelessSpotReplicantsCount = homelessSpotReplicantsCount;
        this.homelessSpotRoomSize = homelessSpotRoomSize;
        this.homelessSpotMaxInSingleGridCell = homelessSpotMaxInSingleGridCell;
        this.homelessSpotPercentOfMale = homelessSpotPercentOfMale;
        this.board.require(Household.class, Person.class, Location.class, Replicant.class);
        this.sexSubPoolM = replicantsPopulation.getPopulation().getPeopleBySex().createSubPool(Person.Sex.M);
        this.sexSubPoolK = replicantsPopulation.getPopulation().getPeopleBySex().createSubPool(Person.Sex.K);
        this.ages = replicantsPopulation.getPopulation().getPeopleByAge().createSubPool(
                AgeRange.AGE_20_24,
                AgeRange.AGE_25_29,
                AgeRange.AGE_30_34,
                AgeRange.AGE_35_39,
                AgeRange.AGE_40_44,
                AgeRange.AGE_45_49,
                AgeRange.AGE_50_54,
                AgeRange.AGE_55_59,
                AgeRange.AGE_60_64,
                AgeRange.AGE_65_69,
                AgeRange.AGE_70_74,
                AgeRange.AGE_75_79,
                AgeRange.AGE_80_
        );
        this.random = randomProvider.getRandomDataGenerator(HomelessSpotUrizen.class);
    }

    public void fabricate() throws FileNotFoundException {
        populationDensityLoader.load();
        int maxSize = homelessSpotMaxInSingleGridCell * homelessSpotRoomSize;
        int count = homelessSpotReplicantsCount;
        while (count > 0) {
            int size = Math.min(random.nextInt(homelessSpotRoomSize, maxSize), count);
            generateHomelessSpot(size);
            count -= size;
        }
    }

    private void generateHomelessSpot(int homelessSpotSize) {
        KilometerGridCell cell = populationDensityLoader.sample(random.getRandomGenerator().nextDouble());
        int leftToCreate = homelessSpotSize;
        while (leftToCreate > 0) {
            int roomSize = Math.min(this.homelessSpotRoomSize, leftToCreate);
            generateRoom(roomSize, cell);
            leftToCreate -= roomSize;
        }
    }

    private void generateRoom(int inhabitants, KilometerGridCell cell) {
        board.getEngine().execute(sessionFactory -> {
            Session session = sessionFactory.create();
            List<Entity> dependents = prototypes.homelessSpotRoom(session, cell).get(Household.class).getMembers();
            Bin<Person.Sex> sex;
            if (random.nextInt(0,99) < this.homelessSpotPercentOfMale) {
                sex = sexSubPoolM.sample(0.5);
            } else {
                sex = sexSubPoolK.sample(0.5);
            }
            for (int i = 0; i < inhabitants; i++) {
                var sexPicked = sex.pick();
                var ageRangePicked = ages.sample(random.getRandomGenerator().nextDouble())
                        .pick();
                dependents.add(prototypes.homelessSpotResident(
                        session,
                        sexPicked,
                        ageSexFromDistributionPicker.getEmpiricalDistributedRandomAge(sexPicked,
                                ageRangePicked, random.getRandomGenerator().nextDouble())));
            }
            session.close();
        });
    }
}
