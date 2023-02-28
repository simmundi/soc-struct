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

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.EngineIoFactory;
import pl.edu.icm.em.common.EmConfig;
import pl.edu.icm.board.model.*;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.util.Status;
import pl.edu.icm.trurl.visnow.VnPointsExporter;

import java.io.IOException;

import static pl.edu.icm.trurl.ecs.util.EntityIterator.select;

public class DistanceFromHealthcareExporter {

    private final EngineIo engineIo;
    private final String healthcareDistanceExportFilename;
    private final Selectors selectors;

    @WithFactory
    public DistanceFromHealthcareExporter(EngineIo engineIo,
                                          String healthcareDistanceExportFilename,
                                          Selectors selectors) {
        this.healthcareDistanceExportFilename = healthcareDistanceExportFilename;
        this.selectors = selectors;
        engineIo.require(Household.class, Location.class, Patient.class, Healthcare.class, Person.class);
        this.engineIo = engineIo;
    }

    public void export() throws IOException {
        Engine engine = engineIo.getEngine();
        var exporter = VnPointsExporter.create(
                ExportedPatient.class,
                healthcareDistanceExportFilename);

        ExportedPatient exported = new ExportedPatient();
        var status = Status.of("Outputing agents", 500_000);
        engine.execute(
                select(selectors.allWithComponents(Household.class, Location.class))
                        .dontPersist()
                        .forEach(Household.class, Location.class, (householdEntity, household, location) -> {
                            for (Entity member : household.getMembers()) {
                                Patient patient = member.get(Patient.class);
                                if (patient != null) {
                                    var healthcareUnit = patient.getHealthcare();
                                    exported.setSex((short) member.get(Person.class).getSex().ordinal());
                                    exported.setX(location.getE() / 1000f);
                                    exported.setY(location.getN() / 1000f);
                                    if (healthcareUnit == null) {
                                        exported.setType((short) 0);
                                        continue;
                                    }
                                    var targetLocation = healthcareUnit.get(Location.class);
                                    var distance = Math.hypot(targetLocation.getE() - location.getE(), targetLocation.getN() - location.getN()) / 1000f;
                                    exported.setDistance((float) distance);
                                    exported.setType((short) 1);
                                } else {
                                    var person = member.get(Person.class);
                                    exported.setSex((short) person.getSex().ordinal());
                                    exported.setX(location.getE() / 1000f);
                                    exported.setY(location.getN() / 1000f);
                                    exported.setType((short) 0);
                                }
                                exporter.append(exported);
                                status.tick();
                            }
                        }));
        exporter.close();
        status.done();
    }

    public static void main(String[] args) throws IOException {
        var config = EmConfig.configurer(args)
                .loadConfigDir("input/config/healthcare")
                .loadConfigDir("input/config/board")
                .getConfig();
        var io = config.get(EngineIoFactory.IT);
        var exporter = config.get(DistanceFromHealthcareExporterFactory.IT);

        io.load("output/added_healthcare.csv");
        exporter.export();
    }
}
