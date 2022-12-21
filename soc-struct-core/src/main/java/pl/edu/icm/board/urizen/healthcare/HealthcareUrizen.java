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

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.model.*;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.trurl.ecs.Session;

import java.io.IOException;

public class HealthcareUrizen {
    private final Board board;
    private final Entities entities;
    private final HealthcareGeodecoder healthcareGeodecoder;
    @WithFactory
    public HealthcareUrizen(
            Board board,
            Entities entities,
            HealthcareGeodecoder healthcareGeodecoder) {
        this.board = board;
        this.entities = entities;
        this.board.require(Location.class);
        this.healthcareGeodecoder = healthcareGeodecoder;
    }

    public void fabricate() {
        try {
            healthcareGeodecoder.foreach(healthcare -> {
                generateHealthcareUnit(healthcare.getPoi().getType(),
                        healthcare.getPoi().getDateOfClosure(),
                        healthcare.getAddressLookupResult().getLocation());
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateHealthcareUnit(HealthcareType type, String dateOfClosure, Location location) {
        if (type == HealthcareType.POZ && dateOfClosure.equals("NULL")) {
            board.getEngine().execute(sessionFactory -> {
                Session session = sessionFactory.create();
                entities.createHealthcare(session, type, location.getN(), location.getE());
                session.close();
            });
        }
    }
}
