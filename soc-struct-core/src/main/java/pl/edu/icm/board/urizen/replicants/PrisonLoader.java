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

package pl.edu.icm.board.urizen.replicants;

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

public class PrisonLoader {
    private final WorkDir workDir;
    private final String prisonFilename;
    public PrisonFromCsvMapper mapper;

    @WithFactory
    public PrisonLoader(WorkDir workDir, @ByName("soc-struct.replicants.prison.source") String prisonFilename) {
        this.workDir = workDir;
        this.prisonFilename = prisonFilename;
    }

    public void load() throws IOException {
        if (mapper == null) {
            mapper = new PrisonFromCsvMapper();
            var status = Status.of("Loading prisons");
            var prisonStore = new ArrayStore(200);
            mapper.configureAndAttach(prisonStore);

            CsvParserSettings csvParserSettings = new CsvParserSettings();
            csvParserSettings.setLineSeparatorDetectionEnabled(true);
            csvParserSettings.setHeaderExtractionEnabled(true);

            CsvParser csvParser = new CsvParser(csvParserSettings);
            AtomicInteger counter = new AtomicInteger(0);
            csvParser.iterateRecords(workDir.openForReading(new File(prisonFilename)), StandardCharsets.UTF_8).forEach(row -> {
                PrisonFromCsv prisonFromCsv = new PrisonFromCsv();
                prisonFromCsv.setName(row.getString("Nazwa zakladu"));
                prisonFromCsv.setLocality(row.getString("miasto"));
                prisonFromCsv.setStreet(row.getString("adres"));
                prisonFromCsv.setPostalCode(row.getString("kod pocztowy"));
                prisonFromCsv.setType(PrisonFromCsv.Type.from(row.getString("plec")));
                prisonFromCsv.setPrisonCount(row.getInt("osadzeni"));
                mapper.ensureCapacity(counter.get());
                mapper.save(prisonFromCsv, counter.get());
                counter.getAndIncrement();
            });
            status.done();
        }
    }

    public void forEach(Consumer<PrisonFromCsv> consumer) {
        try {
            load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int row = 0; row < mapper.getCount(); row++) {
            PrisonFromCsv prisonFromCsv = new PrisonFromCsv();
            mapper.load(null, prisonFromCsv, row);
            consumer.accept(prisonFromCsv);
        }
    }
}
