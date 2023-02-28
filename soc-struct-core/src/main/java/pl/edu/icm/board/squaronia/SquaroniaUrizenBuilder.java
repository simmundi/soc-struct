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

package pl.edu.icm.board.squaronia;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.util.RandomProvider;

public class SquaroniaUrizenBuilder {
    private final EngineIo engineIo;
    private final int familySize;
    private final int populationSize;
    private final int borderLength;
    private final int numberOfWoman;

    @WithFactory
    public SquaroniaUrizenBuilder(EngineIo engineIo,
                                  int defaultFamilySize,
                                  int defaultPopulationSize,
                                  int defaultBorderLength,
                                  float defaultPercentOfWoman) {
        this.engineIo = engineIo;
        this.familySize = defaultFamilySize;
        this.populationSize = defaultPopulationSize;
        this.borderLength = defaultBorderLength;
        this.numberOfWoman = (int) (this.populationSize * defaultPercentOfWoman / 100);
    }

    public SquaroniaUrizen build(RandomProvider randomProvider){
        return new SquaroniaUrizen(engineIo, familySize, populationSize, borderLength, numberOfWoman, randomProvider);
    }
}
