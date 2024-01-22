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

package pl.edu.icm.board.export;

import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.EngineIoFactory;
import pl.edu.icm.em.common.EmConfig;
import pl.edu.icm.em.socstruct.component.Household;
import pl.edu.icm.em.socstruct.component.Person;
import pl.edu.icm.em.socstruct.component.edu.Attendee;
import pl.edu.icm.em.socstruct.component.edu.EducationalInstitution;
import pl.edu.icm.em.socstruct.component.geo.Location;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.util.ActionService;
import pl.edu.icm.trurl.ecs.util.Indexes;
import pl.edu.icm.trurl.ecs.util.IteratingStepBuilder;
import pl.edu.icm.trurl.io.visnow.VnPointsExporter;
import pl.edu.icm.trurl.util.Status;

import java.io.IOException;

public class DistanceFromAttendeeExporter {

    private final EngineIo engineIo;
    private final String odleglosciPath;
    private final Indexes indexes;
    private final ActionService actionService;

    @WithFactory
    public DistanceFromAttendeeExporter(EngineIo engineIo,
                                        @ByName("soc-struct.export.distances") String odleglosciPath,
                                        Indexes indexes, ActionService actionService) {
        this.odleglosciPath = odleglosciPath;
        this.indexes = indexes;
        this.actionService = actionService;
        engineIo.require(Household.class, Location.class, Attendee.class, EducationalInstitution.class, Person.class);
        this.engineIo = engineIo;
    }

    public void export() throws IOException {
        Engine engine = engineIo.getEngine();
        var exporter = VnPointsExporter.create(
                ExportedAttendee.class,
                odleglosciPath);

        ExportedAttendee exported = new ExportedAttendee();
        var statusBar = Status.of("Outputing agents", 500000);

        engine.execute(
                IteratingStepBuilder.iteratingOver(indexes.allWithComponents(Household.class, Location.class))
                        .persisting()
                        .withoutContext()
                        .perform(actionService.withComponents(Household.class, Location.class, (householdEntity, household, location) -> {
                            for (Entity member : household.getMembers()) {
                                Attendee attendee = member.get(Attendee.class);
                                if (attendee != null) {
                                    var educationalInstitution = attendee.getInstitution().get(EducationalInstitution.class);
                                    if (educationalInstitution == null) {
                                        continue;
                                    }
                                    var person = member.get(Person.class);
                                    exported.setSex((short) person.getSex().ordinal());
                                    exported.setX(location.getE() / 1000f);
                                    exported.setY(location.getN() / 1000f);
                                    var targetLocation = attendee.getInstitution().get(Location.class);
                                    var distance = Math.hypot(targetLocation.getE() - location.getE(), targetLocation.getN() - location.getN()) / 1000f;
                                    exported.setDistance((float) distance);
                                    var type = educationalInstitution.getLevel();
                                    if (type != null) {
                                        exported.setType((short) type.ordinal());
                                    } else {
                                        exported.setType((short) -1);
                                    }

                                    exporter.append(exported);
                                }

                                statusBar.tick();
                            }
                        })).build());
        exporter.close();
        statusBar.done();
    }

    public static void main(String[] args) throws IOException {
        var config = EmConfig.create(args);
        var io = config.get(EngineIoFactory.IT);
        var exporter = config.get(DistanceFromAttendeeExporterFactory.IT);

        io.loadOrc("output/5_people_households_edu_assigned.orc");
        exporter.export();
    }
}
