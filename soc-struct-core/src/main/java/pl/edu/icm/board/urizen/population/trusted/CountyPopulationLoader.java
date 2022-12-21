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

package pl.edu.icm.board.urizen.population.trusted;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.trurl.csv.CsvReader;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.store.array.ArrayStoreFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CountyPopulationLoader {
    private final Map<String, BasicPopulationDatum> population;
    private final WorkDir workDir;
    private final int totalPopulation;

    @WithFactory
    public CountyPopulationLoader(
            String powiatsPopulationFilename,
            WorkDir workDir,
            int totalPopulation) {
        this.workDir = workDir;
        this.totalPopulation = totalPopulation;

        try (InputStream inputStream = workDir.openForReading(new File(powiatsPopulationFilename))){
            var loader = new CsvReader();
            population = Mappers.stream(loader.load(
                            inputStream,
                            new ArrayStoreFactory(),
                            BasicPopulationDatum.class,
                            "", "teryt", "total"))
                    .collect(
                            Collectors.toMap(
                                    BasicPopulationDatum::getTeryt,
                                    Function.identity(),
                                    (a, b) -> a));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int populationOf(String teryt) {
        return population.get(teryt).getTotal();
    }

    public int totalPopulation() {
        return totalPopulation;
    }

}
