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

package pl.edu.icm.board.urizen.university;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.em.socstruct.component.NameTag;
import pl.edu.icm.em.socstruct.component.edu.EducationLevel;
import pl.edu.icm.em.socstruct.component.edu.EducationalInstitution;
import pl.edu.icm.em.socstruct.component.geo.AdministrationUnitTag;
import pl.edu.icm.em.socstruct.component.geo.Location;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.util.Status;

public class UniversityEntitiesUrizen {
    private final UniversityLoader universityLoader;
    private final EngineIo engineIo;
    private final Entities entities;
    private final double universityRadius;

    @WithFactory
    public UniversityEntitiesUrizen(UniversityLoader universityLoader,
                                    EngineIo engineIo,
                                    Entities entities,
                                    int universityRadius) {

        this.universityLoader = universityLoader;
        this.engineIo = engineIo;
        this.entities = entities;
        this.universityRadius = universityRadius;

        engineIo.require(
                EducationalInstitution.class,
                Location.class,
                NameTag.class,
                AdministrationUnitTag.class);
    }

    public void buildEntities() {
        var status = Status.of("Building universities");
        engineIo.getEngine().execute(sessionFactory -> {
            Session session = sessionFactory.create();
            var bigUniversities = universityLoader.loadBigUniversities();
            var smallUniversities = universityLoader.loadSmallUniversities();
            for (University bigUniversity : bigUniversities) {
                entities.createEducationInstitution(session,
                        "",
                        bigUniversity.getLocation(),
                        EducationLevel.BU,
                        bigUniversity.getStudentCount(),
                        0,
                        "");
                status.tick();
            }

            for (University smallUniversity : smallUniversities) {
                entities.createEducationInstitution(session,
                        "",
                        smallUniversity.getLocation(),
                        EducationLevel.U,
                        smallUniversity.getStudentCount(),
                        0,
                        "");
                status.tick();
            }
            session.close();
            status.done();
        });
    }
}
