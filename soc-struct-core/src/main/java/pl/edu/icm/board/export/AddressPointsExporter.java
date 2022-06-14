package pl.edu.icm.board.export;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.DefaultConfig;
import pl.edu.icm.board.geography.prg.AddressPointManager;
import pl.edu.icm.trurl.util.Status;
import pl.edu.icm.trurl.visnow.VnPointsExporter;

import java.io.IOException;

public class AddressPointsExporter {
    private final AddressPointManager addressPointManager;

    @WithFactory
    public AddressPointsExporter(AddressPointManager addressPointManager) {
        this.addressPointManager = addressPointManager;
    }

    public void export() throws IOException {
        var exporter = VnPointsExporter.create(
                ExportedAddressPoint.class,
                "output/vn/address_points");

        ExportedAddressPoint exported = new ExportedAddressPoint();
        var statusBar = Status.of("Outputing addressPoints", 1000000);
        addressPointManager.streamAddressPoints()
                .forEach(ap -> {
                    try {
                        exported.setX(ap.getEasting() / 1000);
                        exported.setY(ap.getNorthing() / 1000);
                        exported.setPostalCode(Integer.parseInt(ap.getPostalCode().replaceAll("[-=]", "").strip(), 10));
                        exporter.append(exported);
                        statusBar.tick();
                    } catch (NumberFormatException nfe) {
                        System.out.println("error in postal code: [" + ap.getPostalCode() + "]");
                    }
                });
        exporter.close();
        statusBar.done();
    }

}
