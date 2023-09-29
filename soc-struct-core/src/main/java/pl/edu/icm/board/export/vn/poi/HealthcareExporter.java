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

import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.model.*;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class HealthcareExporter {

    private final EngineIo engineIo;
    private final PoiExporter poiExporter;
    private final String healthcareExportFilename;

    private final static Map<HealthcareType, PoiItem.Type> levelsToTypes = new EnumMap<>(Map.of(
            HealthcareType.POZ, PoiItem.Type.HEALTHCARE_POZ,
            HealthcareType.OTHER, PoiItem.Type.HEALTHCARE_OTHER
    ));

    @WithFactory
    public HealthcareExporter(EngineIo engineIo,
                              PoiExporter poiExporter,
                              @ByName("soc-struct.healthcare.export") String healthcareExportFilename) {
        this.engineIo = engineIo;
        this.poiExporter = poiExporter;
        this.healthcareExportFilename = healthcareExportFilename;

        engineIo.require(Location.class, Healthcare.class);
    }

    public void export() throws IOException {
        var engine = engineIo.getEngine();
        poiExporter.export(healthcareExportFilename,
                engine
                        .streamDetached()
                        .filter(e -> e.optional(Healthcare.class).isPresent()),
                (poiItem, entity) -> {
                    entity.optional(Healthcare.class).ifPresent(ei -> {
                        poiItem.setSubsets(levelsToTypes.get(ei.getType()));
                    });
                    return poiItem;
                });
    }
}
