package pl.edu.icm.board.urizen.household.cloner;

import net.snowyhollows.bento2.annotation.WithFactory;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.model.AdministrationUnit;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Named;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.population.trusted.CountyPopulationLoader;
import pl.edu.icm.board.urizen.replicants.ReplicantsCounter;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.bin.BinPool;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.util.Status;

import java.util.concurrent.atomic.AtomicInteger;

public class HouseholdClonerUrizen {
    private final Board board;
    private final CountyPopulationLoader countyPopulationLoader;
    private final RandomGenerator randomGenerator;
    private final FamilyShapeStatsService familyShapeStatsService;
    private final ReplicantsCounter replicantsCounter;

    @WithFactory
    public HouseholdClonerUrizen(
            Board board,
            CountyPopulationLoader countyPopulationLoader,
            RandomProvider randomProvider,
            FamilyShapeStatsService familyShapeStatsService,
            ReplicantsCounter replicantsCounter) {
        this.countyPopulationLoader = countyPopulationLoader;
        this.board = board;
        this.randomGenerator = randomProvider.getRandomGenerator(HouseholdClonerUrizen.class);
        this.familyShapeStatsService = familyShapeStatsService;
        this.replicantsCounter = replicantsCounter;
        board.require(
                Household.class,
                AdministrationUnit.class,
                Person.class,
                Named.class);
    }

    public void cloneHouseholds() {
        var familyShapeStats = familyShapeStatsService.countStats();
        var householdStatus = Status.of("Cloning households to scale", 100_000);
        double total = countyPopulationLoader.totalPopulation();
        double replicants = replicantsCounter.getReplicantsCount();;
        double nonReplicantRatio = (total - replicants) / total;
        AtomicInteger peopleCreated = new AtomicInteger();
        AtomicInteger householdsCreated = new AtomicInteger();
        familyShapeStats.populationByTeryt.entrySet().forEach(entry -> {
            String teryt = entry.getKey();
            int terytTotalPopulation = countyPopulationLoader.populationOf(teryt);
            int targetNonReplicantPopulation = (int) (terytTotalPopulation * nonReplicantRatio);
            int householdMembersCount = entry.getValue().get();
            int householdMembersToCreate = targetNonReplicantPopulation - householdMembersCount;
            BinPool<HouseholdShape> householdShapes = familyShapeStats.shapesByTeryt.get(teryt);
            board.getEngine().execute(sessionFactory -> {
                Session session = sessionFactory.create();
                int counter = householdMembersToCreate;
                while (counter > 0) {
                    HouseholdShape shape = householdShapes.sample(randomGenerator.nextDouble()).pick();
                    int memberCount = shape.getMemberCount();

                    shape.createHouse(session, randomGenerator);
                    counter -= memberCount;
                    peopleCreated.addAndGet(memberCount);
                    householdsCreated.incrementAndGet();
                    householdStatus.tick();
                }
            });
        });
        householdStatus.done("Created %d new household members in %d households; targeting population of %d, including %d replicants", peopleCreated.get(), householdsCreated.get(), (int)total, (int)replicants);
    }
}
