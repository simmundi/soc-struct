package pl.edu.icm.board.urizen.place;

import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import net.snowyhollows.bento2.Bento;
import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.geography.prg.AddressPointManager;
import pl.edu.icm.board.geography.prg.model.GeocodedPoi;
import pl.edu.icm.board.urizen.place.export.ExportedToCsvEducationalInstitution;
import pl.edu.icm.board.DefaultConfig;
import pl.edu.icm.trurl.util.DefaultFilesystem;
import pl.edu.icm.trurl.util.Filesystem;
import pl.edu.icm.trurl.visnow.VnPointsExporter;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class EducationInstitutionGeodecoder {

    private final EducationalInstitutionLoader educationalInstitutionLoader;
    private final AddressPointManager addressPointManager;

    @WithFactory
    public EducationInstitutionGeodecoder(EducationalInstitutionLoader educationalInstitutionLoader, AddressPointManager addressPointManager) {
        this.educationalInstitutionLoader = educationalInstitutionLoader;
        this.addressPointManager = addressPointManager;
    }

    public void foreach(Consumer<GeocodedPoi<EducationalInstitutionFromCsv>> consumer) throws IOException {
        educationalInstitutionLoader.load();
        educationalInstitutionLoader.forEach(institution -> {
            var results = addressPointManager.lookup(
                    institution.getCommuneTeryt(),
                    institution.getPostalCode(),
                    institution.getLocality(),
                    institution.getStreet(),
                    institution.getStreetNumber()
            );
            consumer.accept(new GeocodedPoi<>(results, institution));
        });
    }
}
