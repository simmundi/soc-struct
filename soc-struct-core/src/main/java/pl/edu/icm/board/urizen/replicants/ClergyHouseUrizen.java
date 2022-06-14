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
import pl.edu.icm.trurl.bin.BinPool;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;

import java.io.FileNotFoundException;
import java.util.List;

public class ClergyHouseUrizen {
    private final Board board;
    private final PopulationDensityLoader populationDensityLoader;
    private final ReplicantPrototypes prototypes;
    private final RandomDataGenerator random;
    private final int clergyHouseReplicantsCount;
    private final int clergyHouseRoomSize;
    private final BinPool<AgeRange> ages;
    private final BinPool<Person.Sex> sexes;
    private final AgeSexFromDistributionPicker ageSexFromDistributionPicker;

    @WithFactory
    public ClergyHouseUrizen(
            Board board,
            ReplicantPrototypes prototypes,
            ReplicantsPopulation replicantsPopulation,
            PopulationDensityLoader populationDensityLoader,
            AgeSexFromDistributionPicker ageSexFromDistributionPicker, int clergyHouseReplicantsCount, int clergyHouseRoomSize, RandomProvider randomProvider) {
        this.board = board;
        this.prototypes = prototypes;
        this.populationDensityLoader = populationDensityLoader;
        this.ageSexFromDistributionPicker = ageSexFromDistributionPicker;
        this.clergyHouseReplicantsCount = clergyHouseReplicantsCount;
        this.clergyHouseRoomSize = clergyHouseRoomSize;
        this.board.require(Household.class, Person.class, Location.class, Replicant.class);
        this.sexes = replicantsPopulation.getPopulation().getPeopleBySex().createSubPool(Person.Sex.M);
        this.ages = replicantsPopulation.getPopulation().getPeopleByAge().createSubPool(
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
        this.random = randomProvider.getRandomDataGenerator(ClergyHouseUrizen.class);
    }

    public void fabricate() throws FileNotFoundException {
        populationDensityLoader.load();
        int maxSize = clergyHouseRoomSize;
        int minSize = 1;
        int count = clergyHouseReplicantsCount;
        int size;
        while (count > 0) {
            size = Math.min(random.nextInt(minSize, maxSize), count);
            generateClergyHouse(size);
            count -= 1;
        }
    }

    private void generateClergyHouse(int roomSize) {
        KilometerGridCell cell = populationDensityLoader.sample(random.getRandomGenerator().nextDouble());
        generateRoom(roomSize, cell);
    }

    private void generateRoom(int inhabitants, KilometerGridCell cell) {
        board.getEngine().execute(sessionFactory -> {
            Session session = sessionFactory.create();
            List<Entity> dependents = prototypes.clergyHouseRoom(session, cell).get(Household.class).getMembers();
            for (int i = 0; i < inhabitants; i++) {
                var sexPicked = sexes.sample(random.getRandomGenerator().nextDouble()).pick();
                var ageRangePicked = ages.sample(random.getRandomGenerator().nextDouble())
                        .pick();
                dependents.add(prototypes.clergyHouseResident(
                        session,
                        sexPicked,
                        ageSexFromDistributionPicker.getEmpiricalDistributedRandomAge(sexPicked,
                                ageRangePicked, random.getRandomGenerator().nextDouble())));
            }
            session.close();
        });
    }
}
