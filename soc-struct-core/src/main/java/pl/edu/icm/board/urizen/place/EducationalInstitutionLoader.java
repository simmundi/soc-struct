package pl.edu.icm.board.urizen.place;

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

public class EducationalInstitutionLoader {
    private final String educationalInstitutionsFilename;
    private static EducationalInstitutionFromCsvMapper mapper;

    @WithFactory
    public EducationalInstitutionLoader(String educationalInstitutionsFilename) {
        this.educationalInstitutionsFilename = educationalInstitutionsFilename;
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
            csvParser.iterateRecords(new File(educationalInstitutionsFilename), StandardCharsets.UTF_8).forEach(row -> {
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
