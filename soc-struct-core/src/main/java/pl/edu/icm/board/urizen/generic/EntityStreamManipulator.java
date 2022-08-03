package pl.edu.icm.board.urizen.generic;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.trurl.bin.BinPoolsByShape;
import pl.edu.icm.trurl.ecs.Entity;

import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

public class EntityStreamManipulator {

    @WithFactory
    public EntityStreamManipulator() {
    }

    public Function<Entity, Stream<KilometerGridCell>> cellsInRadius$(int r) {
        return entity -> KilometerGridCell.fromLocation(entity.get(Location.class))
                .neighboringCircle(r);
    }

    public <SHAPE> BinPoolsByShape<SHAPE, Entity> groupIntoShapes(
            Stream<Entity> entities,
            ToIntFunction<Entity> countExtractor,
            Function<Entity, Stream<SHAPE>> shapeExtractor) {

        return BinPoolsByShape.group(
                entities,
                countExtractor,
                shapeExtractor);
    }

}
