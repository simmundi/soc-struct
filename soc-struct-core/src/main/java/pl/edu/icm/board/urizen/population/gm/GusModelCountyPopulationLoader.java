/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.board.urizen.population.gm;

import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.board.urizen.population.Population;
import pl.edu.icm.board.util.CacheManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GusModelCountyPopulationLoader implements CacheManager.HasCache {

    private final List<Record> records = new ArrayList<>();
    private final WorkDir workDir;
    private final String gmCountyStatsFilename;

    @WithFactory
    public GusModelCountyPopulationLoader(String gmCountyStatsFilename, CacheManager cacheManager, WorkDir workDir) {
        this.gmCountyStatsFilename = gmCountyStatsFilename;
        this.workDir = workDir;
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
        records.addAll(csvParser.parseAllRecords(workDir.openForReading(osoby)));
    }
}
