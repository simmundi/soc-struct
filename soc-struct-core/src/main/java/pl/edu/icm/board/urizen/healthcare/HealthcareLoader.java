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

package pl.edu.icm.board.urizen.healthcare;

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

public class HealthcareLoader {
    private final String healthcareFilename;
    private final WorkDir workDir;
    public HealthcareFromCsvMapper mapper;

    @WithFactory
    public HealthcareLoader(@ByName("soc-struct.healthcare.source") String healthcareFilename,
                            WorkDir workDir) {
        this.healthcareFilename = healthcareFilename;
        this.workDir = workDir;
    }

    public void load() throws IOException {
        if (mapper == null) {
            mapper = new HealthcareFromCsvMapper();
            var status = Status.of("Loading healthcare units");
            var healthcareStore = new ArrayStore(410000);
            mapper.configureStore(healthcareStore);
            mapper.attachStore(healthcareStore);

            CsvParserSettings csvParserSettings = new CsvParserSettings();
            csvParserSettings.setLineSeparatorDetectionEnabled(true);
            csvParserSettings.setHeaderExtractionEnabled(true);

            CsvParser csvParser = new CsvParser(csvParserSettings);
            AtomicInteger counter = new AtomicInteger(0);
            csvParser.iterateRecords(workDir.openForReading(new File(healthcareFilename)), StandardCharsets.UTF_8).forEach(row -> {
                HealthcareFromCsv healthcareFromCsv = new HealthcareFromCsv();
                healthcareFromCsv.setType(row.getString("KodResortVIII"));
                healthcareFromCsv.setCommuneTeryt(row.getString("Teryt"));
                healthcareFromCsv.setStreet(row.getString("Ulica"));
                healthcareFromCsv.setStreetNumber(row.getString("Budynek"));
                healthcareFromCsv.setPostalCode(row.getString("Kod pocztowy"));
                healthcareFromCsv.setLocality(row.getString("Miejscowość"));
                healthcareFromCsv.setDateOfClosure(row.getString("Data zakończenia działalności komórki"));
                mapper.ensureCapacity(counter.get());
                mapper.save(healthcareFromCsv, counter.get());
                counter.getAndIncrement();
            });
            status.done();
        }
    }

    public void forEach(Consumer<HealthcareFromCsv> consumer) {
        try {
            load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int row = 0; row < mapper.getCount(); row++) {
            HealthcareFromCsv healthcareFromCsv = new HealthcareFromCsv();
            mapper.load(null, healthcareFromCsv, row);
            consumer.accept(healthcareFromCsv);
        }
    }
}
