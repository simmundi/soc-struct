package pl.edu.icm.board.urizen.healthcare;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.geography.prg.AddressPointManager;
import pl.edu.icm.board.geography.prg.model.GeocodedPoi;

import java.io.IOException;
import java.util.function.Consumer;

public class HealthcareGeodecoder {

    private final HealthcareLoader healthcareLoader;
    private final AddressPointManager addressPointManager;

    @WithFactory
    public HealthcareGeodecoder(HealthcareLoader healthcareLoader, AddressPointManager addressPointManager) {
        this.healthcareLoader = healthcareLoader;
        this.addressPointManager = addressPointManager;
    }

    public void foreach(Consumer<GeocodedPoi<HealthcareFromCsv>> consumer) throws IOException {
        healthcareLoader.load();
        healthcareLoader.forEach(primaryCare -> {
            var results = addressPointManager.lookup(
                    primaryCare.getCommuneTeryt(),
                    primaryCare.getPostalCode(),
                    primaryCare.getLocality(),
                    primaryCare.getStreet(),
                    primaryCare.getStreetNumber()
            );
            consumer.accept(new GeocodedPoi<>(results, primaryCare));
        });
    }
}
