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

package pl.edu.icm.board.urizen.person;

import com.google.common.base.Charsets;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.trurl.bin.Histogram;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class NamesSource {

    private final String namesFPath;
    private final String namesMPath;
    private final String surnamesPath;
    private final WorkDir workDir;

    @WithFactory
    public NamesSource(
            @ByName("soc-struct.population.names.f") String namesFPath,
            @ByName("soc-struct.population.names.m") String namesMPath,
            @ByName("soc-struct.population.names.surnames") String surnamesPath,
            WorkDir workDir
    ) {
        this.namesFPath = namesFPath;
        this.namesMPath = namesMPath;
        this.surnamesPath = surnamesPath;
        this.workDir = workDir;
        load();
    }

    public NamePools load() {
        try {
            NamePools namePools = new NamePools();
            fill(namePools.maleNames, namesMPath, 0, 2);
            fill(namePools.femaleNames, namesFPath, 0, 2);
            fill(namePools.surnames, surnamesPath, 0, 1);
            return namePools;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void fill(Histogram<String> binPool, String path, int labelColumn, int countColumn) throws FileNotFoundException {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setHeaderExtractionEnabled(true);
        CsvParser csvParser = new CsvParser(settings);

        csvParser.iterateRecords(workDir.openForReading(new File(path)), Charsets.UTF_8).forEach(record -> {
            binPool.add(record.getString(labelColumn), record.getInt(countColumn));
        });

    }
}
