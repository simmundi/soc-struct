package pl.edu.icm.board.urizen.replicants;

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

public class PrisonLoader {
    private final String prisonFilename;
    public PrisonFromCsvMapper mapper;

    @WithFactory
    public PrisonLoader(String prisonFilename) {
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
            csvParser.iterateRecords(new File(prisonFilename), StandardCharsets.UTF_8).forEach(row -> {
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
