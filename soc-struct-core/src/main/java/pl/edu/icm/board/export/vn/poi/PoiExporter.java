package pl.edu.icm.board.export.vn.poi;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.visnow.VnPointsExporter;

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class PoiExporter {

    @WithFactory
    public PoiExporter() {
    }

    public void export(String baseName, Stream<Entity> entityStream, BiFunction<PoiItem, Entity, PoiItem> mapper) throws IOException {
        PoiItem poiItem = new PoiItem();
        var exporter = VnPointsExporter.create(PoiItem.class, baseName);

        entityStream.forEach(entity -> {
            var location = entity.get(Location.class);
            poiItem.setX(location.getE() / 1000.0f);
            poiItem.setY(location.getN() / 1000.0f);
            exporter.append(mapper.apply(poiItem, entity));
        });
        exporter.close();
    }
}
