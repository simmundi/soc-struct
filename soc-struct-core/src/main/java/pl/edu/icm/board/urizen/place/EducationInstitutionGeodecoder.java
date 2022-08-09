package pl.edu.icm.board.urizen.place;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.geography.prg.AddressPointManager;
import pl.edu.icm.board.geography.prg.model.GeocodedPoi;

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
