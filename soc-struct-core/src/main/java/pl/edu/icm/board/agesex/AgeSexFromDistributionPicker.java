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

package pl.edu.icm.board.agesex;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.household.model.AgeRange;
import pl.edu.icm.trurl.bin.BinPool;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

public class AgeSexFromDistributionPicker {
    private final Map<AgeSex, BinPool<Integer>> peopleByAgeSex = new EnumMap<>(AgeSex.class);
    private final WorkDir workDir;

    String ageSexStructureFilename;

    @WithFactory
    public AgeSexFromDistributionPicker(WorkDir workDir, String ageSexStructureFilename) {
        this.workDir = workDir;
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

        var ageSexTail = csvParser.parseAllRecords(workDir.openForReading(ageSexTailFile));

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
