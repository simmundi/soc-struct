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

package pl.edu.icm.board.urizen.healthcare;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.geography.prg.model.AddressLookupResult;
import pl.edu.icm.board.geography.prg.model.GeocodedPoi;
import pl.edu.icm.board.model.HealthcareType;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.SessionFactory;
import pl.edu.icm.trurl.ecs.EntitySystem;

import java.io.IOException;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthcareUrizenTest {
    @Mock
    private Board board;
    @Mock
    private Engine engine;
    @Mock
    private Session session;
    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private Entities entities;
    @Spy
    private AddressLookupResult addressLookupResult = new AddressLookupResult();
    @Spy
    private HealthcareFromCsv healthcare1 = new HealthcareFromCsv();
    @Spy
    private HealthcareFromCsv healthcare2 = new HealthcareFromCsv();
    @Spy
    private HealthcareFromCsv healthcare3 = new HealthcareFromCsv();
    @Mock
    private HealthcareGeodecoder healthcareGeodecoder;

    @BeforeEach
    void before() throws IOException {
        when(board.getEngine()).thenReturn(engine);
        when(sessionFactory.create()).thenReturn(session);
        doAnswer(params -> {
            EntitySystem system = params.getArgument(0);
            system.execute(sessionFactory);
            return null;
        }).when(engine).execute(any());
        addressLookupResult.setLocation(Location.fromPl1992MeterCoords(1, 1));
        healthcare1.setType(HealthcareType.POZ);
        healthcare1.setDateOfClosure("2021-01-20");

        healthcare2.setType(HealthcareType.OTHER);
        healthcare2.setDateOfClosure("NULL");

        healthcare3.setType(HealthcareType.POZ);
        healthcare3.setDateOfClosure("NULL");
        doAnswer(params -> {
            Consumer<GeocodedPoi<HealthcareFromCsv>> consumer = params.getArgument(0);
            consumer.accept(new GeocodedPoi<>(addressLookupResult, healthcare1));
            consumer.accept(new GeocodedPoi<>(addressLookupResult, healthcare2));
            consumer.accept(new GeocodedPoi<>(addressLookupResult, healthcare3));
            return null;
        }).when(healthcareGeodecoder).foreach(any());

    }

    @Test
    @DisplayName("Should create proper number of entities")
    void fabricate() {
        HealthcareUrizen healthcareUrizen = new HealthcareUrizen(board, entities, healthcareGeodecoder);

        // execute
        healthcareUrizen.fabricate();

        // assert
        verify(entities, times(1)).createHealthcare(same(session), any(), anyInt(), anyInt());
    }
}
