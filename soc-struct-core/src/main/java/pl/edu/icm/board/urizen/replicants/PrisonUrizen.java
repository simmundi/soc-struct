package pl.edu.icm.board.urizen.replicants;

import net.snowyhollows.bento2.annotation.WithFactory;
import org.apache.commons.math3.random.RandomDataGenerator;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.agesex.AgeSexFromDistributionPicker;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.*;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.board.urizen.household.model.AgeRange;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.bin.BinPool;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 *
 **/
public class PrisonUrizen {
    private final Board board;
    private final ReplicantPrototypes prototypes;
    private final RandomDataGenerator random;
    private final int prisonRoomSize;
    private final BinPool<AgeRange> ages;
    private final BinPool<Person.Sex> sexes;
    private final BinPool<Person.Sex> sexesM;
    private final BinPool<Person.Sex> sexesK;
    private final AgeSexFromDistributionPicker ageSexFromDistributionPicker;
    private final Entities entities;
    private final PrisonGeodecoder prisonGeodecoder;
    @WithFactory
    public PrisonUrizen(
            Board board,
            ReplicantPrototypes prototypes,
            Entities entities, ReplicantsPopulation replicantsPopulation,
            AgeSexFromDistributionPicker ageSexFromDistributionPicker,
            RandomProvider randomProvider,
            PrisonGeodecoder prisonGeodecoder,
            int prisonRoomSize) {
        this.board = board;
        this.prototypes = prototypes;
        this.entities = entities;
        this.ageSexFromDistributionPicker = ageSexFromDistributionPicker;
        this.prisonRoomSize = prisonRoomSize;
        this.board.require(Household.class, Person.class, Location.class, Replicant.class);
        this.sexes = replicantsPopulation.getPopulation().getPeopleBySex();
        this.sexesM = sexes.createSubPool(Person.Sex.M);
        this.sexesK = sexes.createSubPool(Person.Sex.K);
        this.ages = replicantsPopulation.getPopulation().getPeople20to100();
        this.random = randomProvider.getRandomDataGenerator(PrisonUrizen.class);
        this.prisonGeodecoder = prisonGeodecoder;
    }

    public void fabricate() throws FileNotFoundException {
        try {
            prisonGeodecoder.foreach(geodecoded -> {
                generatePrison(geodecoded.getPoi().getType(), geodecoded.getPoi().getName(),
                        geodecoded.getAddressLookupResult().getLocation(), geodecoded.getPoi().getPrisonCount());
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void generatePrison(PrisonFromCsv.Type type, String name, Location location, int prisonSize) {
        board.getEngine().execute(sessionFactory -> {
            Session session = sessionFactory.create();
            Entity entity = entities.createEmptyComplex(session, prisonSize);
            Complex complex = entity.get(Complex.class);
            complex.setType(Complex.Type.PRISON);
            Named named = new Named();
            named.setName(name);
            entity.add(named);
            entity.add(location);
            List<Entity> complexHouseholds = complex.getHouseholds();
            int leftToCreate = prisonSize;
            while (leftToCreate > 0) {
                int cellSize = Math.min(prisonRoomSize, leftToCreate);
                generateRoom(cellSize, complexHouseholds, session, type, location);
                leftToCreate -= cellSize;
            }
            session.close();
        });
    }

    private void generateRoom(int inhabitants,
                              List<Entity> complexHouseholds,
                              Session session,
                              PrisonFromCsv.Type type,
                              Location location) {
        List<Entity> dependents = prototypes.prisonRoom(session, complexHouseholds, location).get(Household.class).getMembers();
        for (int i = 0; i < inhabitants; i++) {
            var ageRangePicked = ages.sample(random.getRandomGenerator().nextDouble()).pick();
            Person.Sex sexPicked;
            if(type == PrisonFromCsv.Type.PRISON_K) {
                sexPicked = sexesK.sample(random.getRandomGenerator().nextDouble()).pick();
            } else if(type == PrisonFromCsv.Type.PRISON_M) {
                sexPicked = sexesM.sample(random.getRandomGenerator().nextDouble()).pick();
            } else {
                sexPicked = sexes.sample(random.getRandomGenerator().nextDouble()).pick();
            }

            dependents.add(prototypes.prisonResident(
                    session,
                    sexPicked,
                    ageSexFromDistributionPicker.getEmpiricalDistributedRandomAge(sexPicked,
                            ageRangePicked, random.getRandomGenerator().nextDouble())));
        }
    }
}
