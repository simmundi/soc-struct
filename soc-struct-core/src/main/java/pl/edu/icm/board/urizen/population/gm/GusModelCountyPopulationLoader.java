package pl.edu.icm.board.urizen.population.gm;

import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.urizen.population.Population;
import pl.edu.icm.board.util.CacheManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GusModelCountyPopulationLoader implements CacheManager.HasCache {

    private final List<Record> records = new ArrayList<>();
    private final String gmCountyStatsFilename;

    @WithFactory
    public GusModelCountyPopulationLoader(String gmCountyStatsFilename, CacheManager cacheManager) {
        this.gmCountyStatsFilename = gmCountyStatsFilename;
        cacheManager.register(this);
        load();
    }

    public Map<String, Population> createCountyBinPools() {
        if (records.isEmpty()) {
            load();
        }
        Map<String, Population> counties = new HashMap<>();
        for (Record record : records) {
            Population population = new Population(record);
            counties.put(population.getTeryt(), population);
        }
        return counties;
    }

    @Override
    public void free() {
        records.clear();
    }

    @Override
    public void load() {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setHeaderExtractionEnabled(true);
        settings.setDelimiterDetectionEnabled(true);
        CsvParser csvParser = new CsvParser(settings);
        File osoby = new File(gmCountyStatsFilename);
        records.addAll(csvParser.parseAllRecords(osoby));
    }
}
