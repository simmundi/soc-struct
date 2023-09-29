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

package pl.edu.icm.board.urizen.place;

import net.snowyhollows.bento.config.Configurer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.MockRandomProvider;
import pl.edu.icm.board.geography.density.PopulationDensityLoader;
import pl.edu.icm.board.model.*;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.board.urizen.generic.EntityStreamManipulator;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.EngineConfigurationFactory;
import pl.edu.icm.trurl.ecs.util.StaticSelectors;
import pl.edu.icm.trurl.store.tablesaw.TablesawStore;
import pl.edu.icm.trurl.store.tablesaw.TablesawStoreFactory;
import tech.tablesaw.api.Table;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignPatientsToHealthcareUrizenTest {
    private AssignPatientsToHealthcareUrizen assigner;
    EngineIo engineIo;
    @Spy
    private EntityStreamManipulator entityStreamManipulator = new EntityStreamManipulator();
    @Spy
    private Entities entities = new Entities();
    @Spy
    private RandomProvider randomProvider = new MockRandomProvider();
    @Mock
    private PopulationDensityLoader populationDensityLoader;

    @BeforeEach
    void before() throws IOException {
        EngineConfiguration engineConfiguration = new Configurer().setParam("trurl.engine.store-factory", TablesawStoreFactory.class.getName()).getConfig().get(EngineConfigurationFactory.IT);
        engineIo = Mockito.spy(new EngineIo(engineConfiguration, null, null, null));
        StaticSelectors staticSelectors = new StaticSelectors(engineConfiguration);
        assigner = new AssignPatientsToHealthcareUrizen(engineIo, entities, entityStreamManipulator, randomProvider, populationDensityLoader, staticSelectors);
        engineIo.require(Healthcare.class, Location.class, Named.class, Household.class, Person.class, Patient.class);
        engineIo.load(AssignAttendeesToInstitutionsUrizen.class.getResourceAsStream("/healthcareAssignerTest.csv"));
        when(populationDensityLoader.isPopulated(any())).thenReturn(true);
    }

    @Test
    @DisplayName("Should assign people to healthcare units")
    void test () {
        assigner.assignToHealthcare();
        var entities = ((TablesawStore) engineIo.getEngine().getStore()).asTable("entities");

        assertThat(whereAssigned(entities, "9").rowCount()).isEqualTo(2);
        assertThat(whereAssigned(entities, "a").rowCount()).isEqualTo(3);
        assertThat(whereAssigned(entities, "b").rowCount()).isEqualTo(1);
    }

    private Table whereAssigned(Table entities, String id) {
        return entities.where(
                entities.textColumn("healthcare").isEqualTo(id));
    }
}
