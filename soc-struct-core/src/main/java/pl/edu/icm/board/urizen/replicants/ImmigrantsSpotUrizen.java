package pl.edu.icm.board.urizen.replicants;

import net.snowyhollows.bento2.annotation.WithFactory;
import org.apache.commons.math3.random.RandomDataGenerator;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.agesex.AgeSexFromDistributionPicker;
import pl.edu.icm.board.model.AdministrationUnit;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.geography.commune.Commune;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.board.geography.commune.TerytsOfBigCities;
import pl.edu.icm.board.geography.density.PopulationDensityLoader;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Employee;
import pl.edu.icm.board.model.Replicant;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.generic.EntityStreamManipulator;
import pl.edu.icm.board.urizen.household.model.AgeRange;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.board.workplace.ProfessionalActivityAssessor;
import pl.edu.icm.board.model.Workplace;
import pl.edu.icm.trurl.bin.BinPool;
import pl.edu.icm.trurl.bin.BinPoolsByShape;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class ImmigrantsSpotUrizen {
    private final Board board;
    private final PopulationDensityLoader populationDensityLoader;
    private final ReplicantPrototypes prototypes;
    private final RandomDataGenerator random;
    private final int immigrantsSpotReplicantsCount;
    private final int immigrantsSpotRoomSize;
    private final int immigrantsSpotMaxRooms;
    private final BinPool<AgeRange> ages;
    private final BinPool<AgeRange> agesOver18;
    private final BinPool<Person.Sex> sexes;
    private final ProfessionalActivityAssessor professionalActivityAssessor;
    private final CommuneManager communeManager;
    private final EntityStreamManipulator entityStreamManipulator;
    private final TerytsOfBigCities terytsOfBigCities;
    private final AgeSexFromDistributionPicker ageSexFromDistributionPicker;

    @WithFactory
    public ImmigrantsSpotUrizen(
            Board board,
            CommuneManager communeManager,
            EntityStreamManipulator entityStreamManipulator,
            ReplicantPrototypes prototypes,
            ReplicantsPopulation replicantsPopulation,
            PopulationDensityLoader populationDensityLoader,
            RandomProvider randomProvider,
            ProfessionalActivityAssessor professionalActivityAssessor,
            AgeSexFromDistributionPicker ageSexFromDistributionPicker,
            TerytsOfBigCities terytsOfBigCities,
            int immigrantsSpotReplicantsCount, int immigrantsSpotRoomSize, int immigrantsSpotMaxRooms) {
        this.board = board;
        this.prototypes = prototypes;
        this.populationDensityLoader = populationDensityLoader;
        this.immigrantsSpotReplicantsCount = immigrantsSpotReplicantsCount;
        this.immigrantsSpotRoomSize = immigrantsSpotRoomSize;
        this.immigrantsSpotMaxRooms = immigrantsSpotMaxRooms;
        this.board.require(Household.class, Person.class, Location.class, Replicant.class, Workplace.class,
                AdministrationUnit.class, Employee.class);
        this.communeManager = communeManager;
        this.entityStreamManipulator = entityStreamManipulator;
        this.ageSexFromDistributionPicker = ageSexFromDistributionPicker;
        this.sexes = replicantsPopulation.getPopulation().getPeopleBySex();
        this.ages = replicantsPopulation.getPopulation().getPeopleByAge().createSubPool(
                AgeRange.AGE_0_4,
                AgeRange.AGE_5_9,
                AgeRange.AGE_10_14,
                AgeRange.AGE_15_19,
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
        this.agesOver18 = ages.createSubPool(
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
        this.random = randomProvider.getRandomDataGenerator(ImmigrantsSpotUrizen.class);
        this.professionalActivityAssessor = professionalActivityAssessor;
        this.terytsOfBigCities = terytsOfBigCities;
    }

    public void fabricate() throws FileNotFoundException {
        populationDensityLoader.load();
        BinPoolsByShape<Commune, Entity> workplacesByCommunes = entityStreamManipulator.groupIntoShapes(
                board.getEngine().streamDetached()
                        .filter(e -> e.get(Workplace.class) != null),
                e -> e.get(Workplace.class).getEmployees(),
                e -> Stream.of(
                        communeManager
                                .communeForTeryt(
                                        e.get(AdministrationUnit.class).getTeryt())
                                .get()
                )
        );
        int maxSize = immigrantsSpotMaxRooms * immigrantsSpotRoomSize;
        int minSize = immigrantsSpotRoomSize;
        int count = immigrantsSpotReplicantsCount;
        while (count > 0) {
            int size = Math.min(random.nextInt(minSize, maxSize), count);
            generateImmigrantsSpot(size, terytsOfBigCities.getAllTeryts(), workplacesByCommunes);
            count -= size;
        }
    }

    private void generateImmigrantsSpot(int immigrantsSpotSize, List<String> teryts, BinPoolsByShape<Commune, Entity> workplacesByCommunes) {
        List<KilometerGridCell> cells = new ArrayList<>();
        cells.addAll(communeManager.communeForTeryt(teryts
                        .get(random.getRandomGenerator().nextInt(teryts.size())))
                .map(Commune::getCells).orElse(Collections.emptySet()));
        KilometerGridCell cell = cells.get(random.getRandomGenerator().nextInt(cells.size()));
        var commune = communeManager.communeAt(cell);
        var workplaces = workplacesByCommunes.getGroupedBins().get(commune);
        int leftToCreate = immigrantsSpotSize;
        while (leftToCreate > 0) {
            int roomSize = Math.min(random.getRandomGenerator().nextInt(immigrantsSpotRoomSize) + 1, leftToCreate);
            generateRoom(roomSize, cell, workplaces);
            leftToCreate -= roomSize;
        }
    }

    private void generateRoom(int inhabitants, KilometerGridCell cell, BinPool<Entity> workplaces) {
        board.getEngine().execute(sessionFactory -> {
            Session session = sessionFactory.create();
            List<Entity> dependents = prototypes.immigrantsSpotRoom(session, cell).get(Household.class).getMembers();
            AgeRange ageRangePicked = agesOver18.sample(random.getRandomGenerator().nextDouble())
                    .pick();
            for (int i = 0; i < inhabitants; i++) {
                var sexPicked = sexes.sample(random.getRandomGenerator().nextDouble()).pick();
                if (i != 0) {
                    ageRangePicked = ages.sample(random.getRandomGenerator().nextDouble())
                            .pick();
                }
                var prototype = prototypes.immigrantsSpotResident(
                        session,
                        sexPicked,
                        ageSexFromDistributionPicker.getEmpiricalDistributedRandomAge(sexPicked,
                                ageRangePicked, random.getRandomGenerator().nextDouble()));
                boolean isAWorker = professionalActivityAssessor.assess(prototype.get(Person.class));
                if (isAWorker) {
                    var targetWorkplace = workplaces.sample(random.getRandomGenerator().nextDouble()).pick();
                    prototype.add(new Employee()).setWork(targetWorkplace);
                }
                dependents.add(prototype);
                session.close();
            }
        });
    }
}
