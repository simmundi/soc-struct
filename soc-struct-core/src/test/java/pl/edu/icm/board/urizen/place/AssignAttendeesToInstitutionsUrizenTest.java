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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.MockRandomProvider;
import pl.edu.icm.board.model.EducationalInstitution;
import pl.edu.icm.board.education.EducationRadiusProvider;
import pl.edu.icm.board.model.Named;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Attendee;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.board.urizen.generic.EntityStreamManipulator;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.csv.CsvWriter;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.util.StaticSelectors;
import pl.edu.icm.trurl.store.tablesaw.TablesawStore;
import pl.edu.icm.trurl.store.tablesaw.TablesawStoreFactory;
import tech.tablesaw.api.Table;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class AssignAttendeesToInstitutionsUrizenTest {

    Board board;
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
        EngineConfiguration engineConfiguration = new EngineConfiguration();
        engineConfiguration.setStoreFactory(new TablesawStoreFactory());
        board = new Board(engineConfiguration, csvWriter, null, null);
        StaticSelectors staticSelectors = new StaticSelectors(engineConfiguration);
        assigner = new AssignAttendeesToInstitutionsUrizen(board, entityStreamManipulator, entities, randomProvider, radius, staticSelectors);
        board.require(EducationalInstitution.class, Location.class, Named.class, Household.class, Person.class, Attendee.class);
        board.load(AssignAttendeesToInstitutionsUrizen.class.getResourceAsStream("/assignerTest.csv"));
    }

    @Test
    @DisplayName("Should assign attendees to educational institutions")
    void test () {
        assigner.assignToInstitutions();
        var entities = ((TablesawStore)board.getEngine().getStore()).asTable("entities");

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
