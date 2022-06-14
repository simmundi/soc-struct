package pl.edu.icm.board.urizen.replicants;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.geography.prg.AddressPointManager;
import pl.edu.icm.board.geography.prg.model.GeocodedPoi;

import java.io.IOException;
import java.util.function.Consumer;

public class PrisonGeodecoder {

    private final PrisonLoader prisonLoader;
    private final AddressPointManager addressPointManager;

    @WithFactory
    public PrisonGeodecoder(PrisonLoader prisonLoader, AddressPointManager addressPointManager) {
        this.prisonLoader = prisonLoader;
        this.addressPointManager = addressPointManager;
    }

    public void foreach(Consumer<GeocodedPoi<PrisonFromCsv>> consumer) throws IOException {
        prisonLoader.load();
        prisonLoader.forEach(prison -> {
            var results = addressPointManager.lookup(
                    prison.getCommuneTeryt(),
                    prison.getPostalCode(),
                    prison.getLocality(),
                    prison.getStreet(),
                    prison.getStreetNumber()
            );
            consumer.accept(new GeocodedPoi<>(results, prison));
        });
    }
}
