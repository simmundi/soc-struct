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
import org.mockito.Mock;
import org.mockito.Spy;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.MockRandomProvider;
import pl.edu.icm.em.socstruct.component.NameTag;
import pl.edu.icm.em.socstruct.component.edu.EducationalInstitution;
import pl.edu.icm.board.education.EducationRadiusProvider;
import pl.edu.icm.em.socstruct.component.geo.Location;
import pl.edu.icm.em.socstruct.component.Household;
import pl.edu.icm.em.socstruct.component.edu.Attendee;
import pl.edu.icm.em.socstruct.component.Person;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.board.urizen.generic.EntityStreamManipulator;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.csv.CsvWriter;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.EngineConfigurationFactory;
import pl.edu.icm.trurl.ecs.util.StaticSelectors;
import pl.edu.icm.trurl.store.tablesaw.TablesawStore;
import pl.edu.icm.trurl.store.tablesaw.TablesawStoreFactory;
import tech.tablesaw.api.Table;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class AssignAttendeesToInstitutionsUrizenTest {

    EngineIo engineIo;
    @Mock
    CsvWriter csvWriter;
    @Spy
    EntityStreamManipulator entityStreamManipulator = new EntityStreamManipulator();
    @Spy
    Entities entities = new Entities();
    @Spy
    RandomProvider randomProvider = new MockRandomProvider();
    @Spy
    EducationRadiusProvider radius = new EducationRadiusProvider(10, 10, 10, 10, 10);
    AssignAttendeesToInstitutionsUrizen assigner;

    @BeforeEach
    void before() throws IOException {
        EngineConfiguration engineConfiguration = new Configurer().setParam("trurl.engine.store-factory", TablesawStoreFactory.class.getName()).getConfig().get(EngineConfigurationFactory.IT);

        engineIo = new EngineIo(engineConfiguration, csvWriter, null, null);
        StaticSelectors staticSelectors = new StaticSelectors(engineConfiguration);
        assigner = new AssignAttendeesToInstitutionsUrizen(engineIo, entityStreamManipulator, entities, randomProvider, radius, staticSelectors);
        engineIo.require(EducationalInstitution.class, Location.class, NameTag.class, Household.class, Person.class, Attendee.class);
        engineIo.load(AssignAttendeesToInstitutionsUrizen.class.getResourceAsStream("/assignerTest.csv"));
    }

    @Test
    @DisplayName("Should assign attendees to educational institutions")
    void test () {
        assigner.assignToInstitutions();
        var entities = ((TablesawStore) engineIo.getEngine().getStore()).asTable("entities");

        assertThat(whereAttends(entities, "p").rowCount()).isEqualTo(1);
        assertThat(whereAttends(entities, "m").rowCount()).isEqualTo(1);
        assertThat(whereAttends(entities, "n").rowCount()).isEqualTo(2);
        assertThat(whereAttends(entities, "r").rowCount()).isEqualTo(2);
        assertThat(whereAttends(entities, "t").rowCount()).isEqualTo(1);
        assertThat(whereAttends(entities, "s").rowCount()).isEqualTo(1);
    }

    private Table whereAttends(Table entities, String id) {
        return entities.where(
                entities.textColumn("institution").isEqualTo(id)
                        .or(entities.textColumn("secondaryInstitution").isEqualTo(id)));
    }
}
