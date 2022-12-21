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

package pl.edu.icm.board.urizen.replicants;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.urizen.population.Population;
import pl.edu.icm.board.urizen.population.gm.GusModelCountyPopulationLoader;

/**
 * <p>
 * Replicants are people outside of the basic GUS model.
 *
 * <p>
 * They are created alongside their households / workplaces and have
 * some special properties (like never working or studying at the same university).
 *
 * <p>
 * To help keep the population statistics correct, all replicants are created from one common pool
 * of "people" - which itself is based on Wrocław's population from our GUS data (for no special reason).
 */
public class ReplicantsPopulation {
    private final GusModelCountyPopulationLoader gusModelCountyPopulationLoader;
    private final Population population;

    @WithFactory
    public ReplicantsPopulation(GusModelCountyPopulationLoader gusModelCountyPopulationLoader) {
        this.gusModelCountyPopulationLoader = gusModelCountyPopulationLoader;
        population = gusModelCountyPopulationLoader.createCountyBinPools().get("0264");
    }

    public Population getPopulation() {
        return population;
    }
}
