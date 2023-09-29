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

import net.snowyhollows.bento.config.Configurer;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.EngineConfigurationFactory;
import pl.edu.icm.trurl.store.StoreFactory;
import pl.edu.icm.trurl.store.StoreFactoryFactory;
import pl.edu.icm.trurl.store.tablesaw.TablesawStore;
import pl.edu.icm.trurl.store.tablesaw.TablesawStoreFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class SquaroniaUrizenTest {

    @Test
    @DisplayName("should generate specific age structure")
    void withAgeGroupShare(){
        //given
        EngineConfiguration engineConfiguration = new Configurer().setParam("trurl.engine.store-factory", TablesawStoreFactory.class.getName()).getConfig().get(EngineConfigurationFactory.IT);

        EngineIo engineIo = new EngineIo(engineConfiguration, null, null, null);
        RandomProvider randomProvider = new RandomProvider(0);
        var squaroniaUrizen = new SquaroniaUrizen(engineIo,3,100,10,50, randomProvider);

        //execute
        squaroniaUrizen.withAgeGroupShare(AgeRange.AGE_0_4,1).withAgeGroupShare(AgeRange.AGE_15_19,1).build();
        //given
        var entities = ((TablesawStore) engineIo.getEngine().getStore()).asTable("entities");

        //assert
        assertThat(entities.where(
                entities.column("age").isNotMissing().andNot(
                        entities.intColumn("age").isBetweenInclusive(0,4).or(
                                entities.intColumn("age").isBetweenInclusive(15,19)
                        )
                )
        ).rowCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("shoud build squaronia")
    void build(){
        //given
        EngineConfiguration engineConfiguration = new Configurer().setParam("trurl.engine.store-factory", TablesawStoreFactory.class.getName()).getConfig().get(EngineConfigurationFactory.IT);

        EngineIo engineIo = new EngineIo(engineConfiguration, null, null, null);
        RandomProvider randomProvider = new RandomProvider(0);
        var squaroniaUrizen = new SquaroniaUrizen(engineIo,4,100,10,50, randomProvider);
        //execute
        squaroniaUrizen.withAgeGroupShare(AgeRange.AGE_0_4,1).build();
        //given
        var entities = ((TablesawStore) engineIo.getEngine().getStore()).asTable("entities");

        //assert
        assertThat(entities.where(
                entities.column("age").isNotMissing()
        ).rowCount()).isEqualTo(100);

        assertThat(entities.where(
                entities.intColumn("age").isNotMissing().andNot(
                        entities.intColumn("age").isBetweenInclusive(0,4)
                )).rowCount()).isEqualTo(0);

        assertThat(
                entities.where(
                entities.intColumn("age").isNotMissing()
        ).xTabPercents("sex").row(0).getDouble(1)
        ).isCloseTo(0.5, Percentage.withPercentage(3));

        assertThat(entities.where(
                entities.intColumn("n").isNotMissing().andNot(
                        entities.intColumn("n").isBetweenInclusive(0,9).or(
                                entities.intColumn("e").isBetweenInclusive(0,9)
                        )
                )
        ).rowCount()
        ).isEqualTo(0);
    }

}
