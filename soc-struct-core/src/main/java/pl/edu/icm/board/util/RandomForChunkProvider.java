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

package pl.edu.icm.board.util;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import pl.edu.icm.trurl.ecs.selector.Chunk;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class RandomForChunkProvider implements Function<Chunk, RandomGenerator> {
    private final Map<Integer, RandomGenerator> chunkToRandomGeneratorMap = new ConcurrentHashMap<>();
    private final int initialSeed;

    RandomForChunkProvider(int initialSeed) {
        this.initialSeed = initialSeed;
    }

    @Override
    public RandomGenerator apply(Chunk chunk) {
        var chunkId = chunk.getChunkInfo().getChunkId();
        return chunkToRandomGeneratorMap.computeIfAbsent(chunkId, i -> new Well19937c(initialSeed + i*100));
    }
}
