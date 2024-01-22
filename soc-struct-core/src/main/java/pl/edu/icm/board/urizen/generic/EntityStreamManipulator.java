/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.board.urizen.generic;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.em.socstruct.component.geo.Location;
import pl.edu.icm.trurl.bin.HistogramsByShape;
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

    public <SHAPE> HistogramsByShape<SHAPE, Entity> groupIntoShapes(
            Stream<Entity> entities,
            ToIntFunction<Entity> countExtractor,
            Function<Entity, Stream<SHAPE>> shapeExtractor) {

        return HistogramsByShape.group(
                entities,
                countExtractor,
                shapeExtractor);
    }

}
