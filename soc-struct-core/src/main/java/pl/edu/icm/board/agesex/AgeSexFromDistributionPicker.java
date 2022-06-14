package pl.edu.icm.board.agesex;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.household.model.AgeRange;
import pl.edu.icm.trurl.bin.BinPool;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

public class AgeSexFromDistributionPicker {
    private final Map<AgeSex, BinPool<Integer>> peopleByAgeSex = new EnumMap<>(AgeSex.class);

    String ageSexStructureFilename;

    @WithFactory
    public AgeSexFromDistributionPicker(String ageSexStructureFilename) {
        this.ageSexStructureFilename = ageSexStructureFilename;
        prepareBinPools();
    }

    private void prepareBinPools() {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setHeaderExtractionEnabled(true);
        settings.setDelimiterDetectionEnabled(true);
        settings.setLineSeparatorDetectionEnabled(true);

        CsvParser csvParser = new CsvParser(settings);
        var ageSexTailFile = new File(ageSexStructureFilename);

        var ageSexTail = csvParser.parseAllRecords(ageSexTailFile);

        ageSexTail.forEach(record -> {
            var age = record.getInt("age");
            var femalesCount = record.getInt("K");
            var malesCount = record.getInt("M");
            var ageSexK = AgeSex.fromAgeSex(age, Person.Sex.K);
            var ageSexM = AgeSex.fromAgeSex(age, Person.Sex.M);
            peopleByAgeSex.putIfAbsent(ageSexK, new BinPool<>());
            peopleByAgeSex.putIfAbsent(ageSexM, new BinPool<>());
            peopleByAgeSex.get(ageSexK).add(age, femalesCount);
            peopleByAgeSex.get(ageSexM).add(age, malesCount);
        });
    }

    public int getEmpiricalDistributedRandomAge(AgeSex ageSex, double random) {
        return peopleByAgeSex.get(ageSex).sample(random).pick();
    }

    public int getEmpiricalDistributedRandomAge(Person.Sex sex, AgeRange ageRange, double random) {
        var ageSex = AgeSex.fromAgeRangeSex(ageRange, sex);
        return getEmpiricalDistributedRandomAge(ageSex, random);
    }
}
