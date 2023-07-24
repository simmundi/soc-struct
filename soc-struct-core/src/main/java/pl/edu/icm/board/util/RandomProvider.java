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

import net.snowyhollows.bento.annotation.WithFactory;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * Extension point for adding controlled randomness
 */
public class RandomProvider {
    private final int randomProviderSeed;
    private final Map<String, RandomDataGenerator> randomGeneratorMap;
    private final Map<String, RandomForChunkProvider> randomDataForChunkProviderMap;

    @WithFactory
    public RandomProvider(int randomProviderSeed) {
        this.randomProviderSeed = randomProviderSeed;
        this.randomGeneratorMap = new HashMap<>();
        this.randomDataForChunkProviderMap = new HashMap<>();
    }

    public RandomDataGenerator getRandomDataGenerator(String label) {
        return this.randomGeneratorMap.computeIfAbsent(label, k -> {
            RandomDataGenerator random = new RandomDataGenerator();
            random.reSeed(this.randomProviderSeed + this.randomGeneratorMap.size());
            return random;
        });
    }

    public RandomDataGenerator getRandomDataGenerator(Class<?> label) {
        return getRandomDataGenerator(label.getCanonicalName());
    }

    public RandomGenerator getRandomGenerator(String label) {
        return getRandomDataGenerator(label).getRandomGenerator();
    }

    public RandomGenerator getRandomGenerator(Class<?> label) {
        return getRandomGenerator(label.getCanonicalName());
    }

    private RandomForChunkProvider getRandomForChunkProvider(String label) {
        return this.randomDataForChunkProviderMap.computeIfAbsent(label, i -> new RandomForChunkProvider(this.randomProviderSeed + this.randomGeneratorMap.size()));
    }

    public RandomForChunkProvider getRandomForChunkProvider(Class<?> label) {
        return getRandomForChunkProvider(label.getCanonicalName());
    }
}
