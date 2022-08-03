package pl.edu.icm.board.urizen.household;

import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.agesex.AgeSexFromDistributionPicker;
import pl.edu.icm.board.model.AdministrationUnit;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.board.urizen.household.model.AgeRange;
import pl.edu.icm.board.urizen.population.Population;
import pl.edu.icm.board.urizen.population.gm.GusModelCountyPopulationLoader;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.bin.BinPool;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.util.Status;

import java.io.File;
import java.util.List;
import java.util.Map;

public class HouseholdUrizen {

    private final WorkDir workDir;
    private String gmHouseholdStatsFilename;
    private final GusModelCountyPopulationLoader gusModelCountyPopulationLoader;
    private final Entities entities;
    private final RandomGenerator random;
    private final RandomDataGenerator randomData;
    private final Board board;
    private final String prefiksTerytu;
    private final GammaDistribution gammaDistribution;
    private final AgeSexFromDistributionPicker ageSexFromDistributionPicker;

    @WithFactory
    public HouseholdUrizen(
            WorkDir workDir, String gmHouseholdStatsFilename,
            GusModelCountyPopulationLoader gusModelCountyPopulationLoader,
            AgeSexFromDistributionPicker ageSexFromDistributionPicker, Entities entities,
            Board board,
            String prefiksTerytu,
            RandomProvider randomProvider,
            float gammaShape,
            float gammaScale) {
        this.workDir = workDir;
        this.gmHouseholdStatsFilename = gmHouseholdStatsFilename;
        this.gusModelCountyPopulationLoader = gusModelCountyPopulationLoader;
        this.ageSexFromDistributionPicker = ageSexFromDistributionPicker;
        this.entities = entities;
        this.board = board;
        this.prefiksTerytu = prefiksTerytu;
        this.board.require(Person.class, Household.class, AdministrationUnit.class);
        this.random = randomProvider.getRandomGenerator(HouseholdUrizen.class);
        this.randomData = new RandomDataGenerator(random);
        this.gammaDistribution = new GammaDistribution(this.randomData.getRandomGenerator(),
                                                       gammaShape,
                                                       gammaScale);
    }

    public void createHouseholds() {
        File mieszkania = new File(gmHouseholdStatsFilename);

        CsvParserSettings settings = new CsvParserSettings();
        settings.setHeaderExtractionEnabled(true);
        settings.setDelimiterDetectionEnabled(true);
        settings.setLineSeparatorDetectionEnabled(true);

        Map<String, Population> populations = gusModelCountyPopulationLoader.createCountyBinPools();

        board.getEngine().execute((sessionFactory) -> {
            CsvParser parser = new CsvParser(settings);
            Status stats = Status.of("Creating households", 1000000);
            Session session = sessionFactory.create();
            for (Record record : parser.iterateRecords(workDir.openForReading(mieszkania))) {
                if (session.getCount() > 200_000) {
                    session.close();
                    session = sessionFactory.create();
                }
                String teryt = record.getString("powiat");
                if (!teryt.startsWith(prefiksTerytu)) {
                    continue;
                }

                Entity entity = entities.createEmptyHousehold(session, teryt);

                int ile80p = record.getInt("ile_osob_w_mieszkaniu_80_plus");
                int ile75p = record.getInt("ile_osob_w_mieszkaniu_75_plus");
                int ile70p = record.getInt("ile_osob_w_mieszkaniu_70_plus");
                int ile65p = record.getInt("ile_osob_w_mieszkaniu_65_plus");
                int ile60p = record.getInt("ile_osob_w_mieszkaniu_60_plus");
                int ile = record.getInt("ile_osob_w_mieszkaniu");
                if (ile >= 10) {
                    ile = tailSizeForHouseholdAbove9();
                }

                // decoding data
                int count80plus = ile80p;
                int count75to80 = ile75p - ile80p;
                int count70to75 = ile70p - ile75p;
                int count65to70 = ile65p - ile70p;
                int count60to65 = ile60p - ile65p;
                int count00to60 = ile - ile60p;
                int count20to60 = 0;

                // single business rule: at least one adult per household
                if (ile60p == 0 && count00to60 > 0) {
                    count00to60--;
                    count20to60++;
                }

                Population population = populations.get(teryt);

                List<Entity> housemates = entity.get(Household.class).getMembers();

                addHouseholdMembers(session, housemates, count80plus, population, population.getPeople80andMore());
                addHouseholdMembers(session, housemates, count75to80, population, population.getPeople75to80());
                addHouseholdMembers(session, housemates, count70to75, population, population.getPeople70to75());
                addHouseholdMembers(session, housemates, count65to70, population, population.getPeople65to70());
                addHouseholdMembers(session, housemates, count60to65, population, population.getPeople60to65());
                addHouseholdMembers(session, housemates, count00to60, population, population.getPeople0to60());
                addHouseholdMembers(session, housemates, count20to60, population, population.getPeople20to60());

                stats.tick();
            }
            session.close();
            stats.done();
        });
    }

    private void addHouseholdMembers(Session ctx, List<Entity> membersList, int count, Population population, BinPool<AgeRange> wiek) {
        while (count-- > 0) {
            Person.Sex sex = population.getPeopleBySex().sample(random.nextDouble()).pick();
            AgeRange ageRangePicked = wiek.sample(random.nextDouble()).pick();
            int age = ageSexFromDistributionPicker.getEmpiricalDistributedRandomAge(sex,ageRangePicked, random.nextDouble());
            Entity citizen = entities.createCitizen(ctx, sex, age);
            membersList.add(citizen);
        }
    }

    private int tailSizeForHouseholdAbove9() {
        int size = 0;
        while (size < 10) {
            size = (int) gammaDistribution.sample() + 1;
        }
        return size;
    }
}
