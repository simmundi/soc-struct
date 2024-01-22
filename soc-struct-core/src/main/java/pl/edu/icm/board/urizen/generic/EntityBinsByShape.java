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

import pl.edu.icm.em.common.math.histogram.Histogram;
import pl.edu.icm.trurl.ecs.Entity;

import java.util.Map;

public final class EntityBinsByShape<SHAPE> {
    private final Histogram<Entity> allBins;
    private final Map<SHAPE, Histogram<Entity>> groupedBins;

    public EntityBinsByShape(Histogram<Entity> allBins, Map<SHAPE, Histogram<Entity>> groupedBins) {
        this.allBins = allBins;
        this.groupedBins = groupedBins;
    }

    public Histogram<Entity> getAllBins() {
        return allBins;
    }

    public Map<SHAPE, Histogram<Entity>> getGroupedBins() {
        return groupedBins;
    }
}
