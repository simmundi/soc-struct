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

package pl.edu.icm.board.urizen.place;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.trurl.store.array.ArrayStore;
import pl.edu.icm.trurl.util.Status;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class EducationalInstitutionLoader {
    private final String educationalInstitutionsFilename;
    private final WorkDir workDir;
    private static EducationalInstitutionFromCsvMapper mapper;

    @WithFactory
    public EducationalInstitutionLoader(@ByName("soc-struct.educational-institutions.source") String educationalInstitutionsFilename,
                                        WorkDir workDir) {
        this.educationalInstitutionsFilename = educationalInstitutionsFilename;
        this.workDir = workDir;
    }

    public void load() throws IOException {
        if (mapper == null) {
            mapper = new EducationalInstitutionFromCsvMapper();
            var status = Status.of("Loading educational institutions");
            var educationalInstitutionsStore = new ArrayStore(35000);
            mapper.configureStore(educationalInstitutionsStore);
            mapper.attachStore(educationalInstitutionsStore);

            CsvParserSettings csvParserSettings = new CsvParserSettings();
            csvParserSettings.setLineSeparatorDetectionEnabled(true);
            csvParserSettings.setHeaderExtractionEnabled(true);

            CsvParser csvParser = new CsvParser(csvParserSettings);
            AtomicInteger counter = new AtomicInteger(0);
            csvParser.iterateRecords(workDir.openForReading(new File(educationalInstitutionsFilename)), StandardCharsets.UTF_8).forEach(row -> {
                EducationalInstitutionFromCsv educationalInstitutionFromCsv = new EducationalInstitutionFromCsv();
                educationalInstitutionFromCsv.setTeachers(row.getInt("LiczbaNaucz"));
                educationalInstitutionFromCsv.setPupils(row.getInt("dzienna"));
                educationalInstitutionFromCsv.setCommuneTeryt(row.getString("idTerytGmina"));
                educationalInstitutionFromCsv.setLocality(row.getString("Korespondencja - miejscowość"));
                educationalInstitutionFromCsv.setName(row.getString("Nazwa placówki"));
                educationalInstitutionFromCsv.setStreet(row.getString("Korespondencja - ulica"));
                educationalInstitutionFromCsv.setPostalCode(row.getString("Korespondencja - kod pocztowy"));
                educationalInstitutionFromCsv.setStreetNumber(row.getString("Korespondencja - numer domu"));
                educationalInstitutionFromCsv.setLevel(EducationalInstitutionFromCsv.Level.from(row.getString("Typ podmiotu")));
                mapper.ensureCapacity(counter.get());
                mapper.save(educationalInstitutionFromCsv, counter.get());
                counter.getAndIncrement();
            });
            mapper.setCount(counter.get());
            status.done();
        }
    }

    public void forEach(Consumer<EducationalInstitutionFromCsv> consumer) {
        try {
            load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int row = 0; row < mapper.getCount(); row++) {
            EducationalInstitutionFromCsv educationalInstitutionFromCsv = new EducationalInstitutionFromCsv();
            mapper.load(null, educationalInstitutionFromCsv, row);
            consumer.accept(educationalInstitutionFromCsv);
        }
    }
}
