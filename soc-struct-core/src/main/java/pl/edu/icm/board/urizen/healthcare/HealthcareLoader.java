package pl.edu.icm.board.urizen.healthcare;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.trurl.store.array.ArrayStore;
import pl.edu.icm.trurl.util.Status;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class HealthcareLoader {
    private final String healthcareFilename;
    public HealthcareFromCsvMapper mapper;

    @WithFactory
    public HealthcareLoader(String healthcareFilename) {
        this.healthcareFilename = healthcareFilename;
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
            csvParser.iterateRecords(new File(healthcareFilename), StandardCharsets.UTF_8).forEach(row -> {
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
