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

package pl.edu.icm.board.export.vn.poi;

import com.google.common.util.concurrent.AtomicLongMap;
import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.model.Complex;
import pl.edu.icm.board.model.EducationLevel;
import pl.edu.icm.board.model.EducationalInstitution;
import pl.edu.icm.board.export.vn.poi.PoiItem.Type;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Attendee;
import pl.edu.icm.board.model.Employee;
import pl.edu.icm.board.model.Replicant;
import pl.edu.icm.board.model.Workplace;
import pl.edu.icm.trurl.util.Status;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class PoiReports {

    private final EngineIo engineIo;
    private final PoiExporter poiExporter;

    private final static Map<EducationLevel, Type> levelsToTypes = new EnumMap<>(Map.of(
            EducationLevel.K, Type.EDU_PRESCHOOL,
            EducationLevel.P, Type.EDU_PRIMARY,
            EducationLevel.H, Type.EDU_HIGH,
            EducationLevel.PH, Type.EDU_PRIMARY_AND_HIGH,
            EducationLevel.U, Type.EDU_UNIVERSITY,
            EducationLevel.BU, Type.EDU_UNIVERSITY
    ));

    private final static Map<Complex.Type, Type> complexTypesToTypes = new EnumMap<>(Map.of(
            Complex.Type.BARRACKS, Type.BARRACKS,
            Complex.Type.CLERGY_HOUSE, Type.CLERGY_HOUSE,
            Complex.Type.DORM, Type.DORM,
            Complex.Type.MONASTERY, Type.MONASTERY,
            Complex.Type.NURSING_HOME, Type.NURSING_HOME,
            Complex.Type.PRISON, Type.PRISON
    ));
    private final String educationFilename;
    private final String workplacesFilename;
    private final String othersFilename;

    @WithFactory
    public PoiReports(EngineIo engineIo,
                      PoiExporter poiExporter,
                      @ByName("soc-struct.export.visnow.education-filename") String educationFilename,
                      @ByName("soc-struct.export.visnow.workplaces-filename") String workplacesFilename,
                      @ByName("soc-struct.export.visnow.others-filename") String othersFilename) {
        this.engineIo = engineIo;
        this.poiExporter = poiExporter;
        this.educationFilename = educationFilename;
        this.workplacesFilename = workplacesFilename;
        this.othersFilename = othersFilename;
        engineIo.require(
                Household.class,
                EducationalInstitution.class,
                Location.class,
                Workplace.class,
                Replicant.class,
                Complex.class,
                Attendee.class,
                Employee.class);
    }

    public void generateReports() throws IOException {
        var slotsTaken = AtomicLongMap.<Integer>create();

        var engine = engineIo.getEngine();
        var status = Status.of("aggregating attendees and employees in pois", 1_000_000);
        engine.streamDetached().forEach(entity -> {
            entity
                    .optional(Attendee.class)
                    .ifPresent(attendee -> {
                        var primary = attendee.getInstitution();
                        var secondary = attendee.getSecondaryInstitution();
                        if (primary != null) {
                            slotsTaken.addAndGet(primary.getId(), 1);
                            status.tick();
                        }
                        if (secondary != null) {
                            slotsTaken.addAndGet(secondary.getId(), 1);
                            status.tick();
                        }
                    });
            entity.optional(Employee.class).ifPresent(employee -> {
                slotsTaken.addAndGet(employee.getWork().getId(), 1);
                status.tick();
            });
        });
        status.done();

        poiExporter.export(educationFilename,
                engine
                        .streamDetached()
                        .filter(e -> e.optional(EducationalInstitution.class).isPresent()),
                (poiItem, entity) -> {
                    entity.optional(EducationalInstitution.class).ifPresent(ei -> {
                        poiItem.setSubsets(levelsToTypes.get(ei.getLevel()));
                        poiItem.setSlots(ei.getPupilCount());
                        poiItem.setTaken((int) slotsTaken.get(entity.getId()));
                    });
                    return poiItem;
                });

        poiExporter.export(workplacesFilename,
                engine
                        .streamDetached()
                        .filter(e -> e.optional(Workplace.class).isPresent()),
                (poiItem, entity) -> {
                    entity.optional(Workplace.class).ifPresent(workplace -> {
                        poiItem.setSubsets(Type.WORKPLACE);
                        poiItem.setSlots(workplace.getEmployees());
                        poiItem.setTaken((int) slotsTaken.get(entity.getId()));
                    });
                    return poiItem;
                });

        poiExporter.export(othersFilename,
                engine
                        .streamDetached()
                        .filter(e -> e.optional(Complex.class).isPresent()),
                (poiItem, entity) -> {
                    entity.optional(Complex.class).ifPresent(complex -> {
                        poiItem.setSubsets(complexTypesToTypes.get(complex.getType()));
                        poiItem.setSlots(complex.getSize());
                        int taken = complex
                                .getHouseholds().stream().map(e -> e.get(Household.class))
                                .mapToInt(h -> h.getMembers().size())
                                .sum();
                        poiItem.setTaken(taken);
                    });
                    return poiItem;
                });
    }
}
