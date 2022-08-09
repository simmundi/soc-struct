package pl.edu.icm.board.export;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.model.EducationalInstitution;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.trurl.bin.BinPool;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.util.Status;
import pl.edu.icm.trurl.visnow.VnPointsExporter;

import java.io.IOException;

public class EducationalInstitutionsExporter {

    @WithFactory
    public EducationalInstitutionsExporter() {

    }

    public void export(BinPool<Entity> institutions) throws IOException {
        var exporter = VnPointsExporter.create(
                ExportedEducationalInstitution.class,
                "output/instytucje_edukacyjne");

        ExportedEducationalInstitution exported = new ExportedEducationalInstitution();
        var statusBar = Status.of("Outputing institutions", 5000);
        institutions.streamBins().forEach(bin -> {
            var location = bin.getLabel().get(Location.class);
            var educationalInstitution = bin.getLabel().get(EducationalInstitution.class);
            var level = educationalInstitution.getLevel();

            exported.setId((short) bin.getLabel().getId());
            exported.setX(location.getE() / 1000f);
            exported.setY(location.getN() / 1000f);
            exported.setCapacity(educationalInstitution.getPupilCount());
            exported.setLeftEmpty((short) bin.getCount());
            exported.setType((short) (level != null ? level.ordinal() : -1));

            exporter.append(exported);
            statusBar.tick();
        });
        exporter.close();
        statusBar.done();
    }
}
