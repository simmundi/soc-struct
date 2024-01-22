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

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.em.socstruct.component.NameTag;
import pl.edu.icm.em.socstruct.component.edu.EducationLevel;
import pl.edu.icm.em.socstruct.component.edu.EducationalInstitution;
import pl.edu.icm.em.socstruct.component.geo.Location;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.em.socstruct.component.geo.AdministrationUnitTag;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.util.Status;

import java.io.IOException;

public class EducationalInstitutionEntitiesUrizen {

    private final EducationInstitutionGeodecoder educationInstitutionGeodecoder;
    private final EngineIo engineIo;
    private final Entities entities;

    @WithFactory
    public EducationalInstitutionEntitiesUrizen(EducationInstitutionGeodecoder educationInstitutionGeodecoder, EngineIo engineIo, Entities entities) {
        this.educationInstitutionGeodecoder = educationInstitutionGeodecoder;
        this.engineIo = engineIo;
        this.entities = entities;

        this.engineIo.require(EducationalInstitution.class, NameTag.class, Location.class, AdministrationUnitTag.class);
    }

    public void buildEntities() {
        var status = Status.of("Adding institutions");
        engineIo.getEngine().execute(sessionFactory -> {
            Session session = sessionFactory.create();
            try {
                educationInstitutionGeodecoder.foreach(geodecoded -> {
                    EducationLevel level = EducationalInstitutionFromCsv.fromLevel(geodecoded.getPoi().getLevel());
                    if (level != null) {
                        entities.createEducationInstitution(
                                session,
                                geodecoded.getPoi().getName(),
                                geodecoded.getAddressLookupResult().getLocation(),
                                level,
                                geodecoded.getPoi().getPupils(),
                                geodecoded.getPoi().getTeachers(),
                                geodecoded.getPoi().getCommuneTeryt());
                    }
                    session.close();
                    status.tick();
                });
                status.done();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
