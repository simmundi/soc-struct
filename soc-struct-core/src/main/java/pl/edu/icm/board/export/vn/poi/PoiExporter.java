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
