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

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.ecs.selector.ChunkInfo;

import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RandomProviderTest {

    @Mock
    Chunk chunk;
    @Mock
    ChunkInfo chunkInfo;

    @Test
    @DisplayName("Should generate the same number repeatedly for a given seed")
    void test() {
        for (int i = -100; i < 100; i++) {
            RandomProvider randomProvider = new RandomProvider(i);
            var random = new RandomDataGenerator(randomProvider.getRandomGenerator(RandomProviderTest.class));
            var a = random.nextUniform(0, 1);
            var b = random.nextInt(0, 1287465);
            var c = random.nextInt(0, 122222);
            for (int j = 0; j < 100; j++) {
                randomProvider = new RandomProvider(i);
                random = new RandomDataGenerator(randomProvider.getRandomGenerator(RandomProviderTest.class));
                assertEquals(a, random.nextUniform(0, 1));
                assertEquals(b, random.nextInt(0, 1287465));
                assertEquals(c, random.nextInt(0, 122222));

            }
        }
    }

    @Test
    void getRandomForChunkProvider() {
        //given
        when(chunk.getChunkInfo()).thenReturn(chunkInfo);
        when(chunkInfo.getChunkId()).thenReturn(0);

        RandomProvider randomProvider = new RandomProvider(2137);
        //execute
        var randomForChunkProvider = randomProvider.getRandomForChunkProvider(RandomProviderTest.class);
        var randomGenerator = randomForChunkProvider.apply(chunk);
        var doubleArray = IntStream.range(0, 20).mapToDouble(i -> randomGenerator.nextDouble()).toArray();
        var randomGenerator2 = new Well19937c(2137 + RandomProviderTest.class.getCanonicalName().hashCode());
        //assert
        assertThat(IntStream.range(0, 20).mapToDouble(i -> randomGenerator2.nextDouble()).toArray()).containsExactly(doubleArray);
    }
}