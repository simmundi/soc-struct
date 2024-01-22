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

package pl.edu.icm.board.pdyn1;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.board.model.*;
import pl.edu.icm.em.socstruct.component.Household;
import pl.edu.icm.em.socstruct.component.Person;
import pl.edu.icm.em.socstruct.component.edu.Attendee;
import pl.edu.icm.em.socstruct.component.edu.EducationLevel;
import pl.edu.icm.em.socstruct.component.edu.EducationalInstitution;
import pl.edu.icm.em.socstruct.component.geo.AdministrationUnitTag;
import pl.edu.icm.em.socstruct.component.geo.Location;
import pl.edu.icm.em.socstruct.component.work.Employee;
import pl.edu.icm.em.socstruct.component.work.Workplace;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.em.common.DebugTextFileService;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.selector.Selector;
import pl.edu.icm.trurl.ecs.util.ArraySelector;
import pl.edu.icm.trurl.util.Status;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pl.edu.icm.trurl.ecs.util.EntityIterator.select;

/**
 * Saves the current state of the engine
 * as a set of .dat files for pdyn 1.5
 * (in the original pdyn format)
 */
public class PdynExporter {
    private final DebugTextFileService debugTextFileService;
    private final WorkDir workDir;
    private final EngineIo engineIo;
    private final Int2ObjectOpenHashMap<IntList> attendees = new Int2ObjectOpenHashMap<>(2_000_000);
    private final CommuneManager communeManager;
    private final PdynIdExporter idExporter;
    private final boolean removeEmptyEduInstitutions;

    private final RandomGenerator random;
    @WithFactory
    public PdynExporter(DebugTextFileService debugTextFileService,
                        WorkDir workDir,
                        EngineIo engineIo,
                        CommuneManager communeManager,
                        @ByName("soc-struct.educational-institutions.remove-empty") boolean removeEmptyEduInstitutions,
                        PdynIdExporter idExporter,
                        RandomProvider randomProvider) {
        this.debugTextFileService = debugTextFileService;
        this.workDir = workDir;
        this.engineIo = engineIo;
        this.communeManager = communeManager;
        this.idExporter = idExporter;
        this.removeEmptyEduInstitutions = removeEmptyEduInstitutions;
        this.random = randomProvider.getRandomGenerator(PdynExporter.class);
        engineIo.require(Household.class,
                Person.class,
                Workplace.class,
                Location.class,
                Attendee.class,
                Employee.class,
                AdministrationUnitTag.class,
                EducationalInstitution.class);
    }

    public void export(String dir) throws IOException {
        var countGd = new AtomicInteger();
        var countAgenci = new AtomicInteger();
        var countZaklady = new AtomicInteger();
        var countKindergartens = new AtomicInteger();
        var countPrimarySchools = new AtomicInteger();
        var countHighSchools = new AtomicInteger();
        var countSmallUniversities = new AtomicInteger();
        var countBigUniversities = new AtomicInteger();

        var selectorZakladyBuilder = new ArraySelector(10_000_000);
        var selectorGdBuilder = new ArraySelector(20_000_000);
        var selectorKindertardensBuilder = new ArraySelector(50_000);
        var selectorPrimarySchoolsBuilder = new ArraySelector(50_000);
        var selectorHighSchoolBuilder = new ArraySelector(50_000);
        var selectorBigUniversitiesBuilder = new ArraySelector(1_000);
        var selectorSmallUniversitiesBuilder = new ArraySelector(1_000);

        var statusCount = Status.of("Counting agents, households and workplaces, edu institutions and universities", 1_000_000);
        householdsAndWorkplacesAndEduInstitutions$().forEach(e -> {
            if (e.get(Household.class) != null) {
                countGd.incrementAndGet();
                countAgenci.addAndGet(e.get(Household.class).getMembers().size());
                selectorGdBuilder.add(e.getId());
            } else if (e.get(Workplace.class) != null) {
                countZaklady.incrementAndGet();
                selectorZakladyBuilder.add(e.getId());
            } else if (e.get(EducationalInstitution.class) != null) {
                EducationLevel eduLevelTmp = e.get(EducationalInstitution.class).getLevel();
                switch (eduLevelTmp.name()) {
                    case "K":
                        countKindergartens.incrementAndGet();
                        selectorKindertardensBuilder.add(e.getId());
                        break;
                    case "P":
                        countPrimarySchools.incrementAndGet();
                        selectorPrimarySchoolsBuilder.add(e.getId());
                        break;
                    case "H":
                        countHighSchools.incrementAndGet();
                        selectorHighSchoolBuilder.add(e.getId());
                        break;
                    case "U":
                        countSmallUniversities.incrementAndGet();
                        selectorSmallUniversitiesBuilder.add(e.getId());
                        break;
                    case "BU":
                        countBigUniversities.incrementAndGet();
                        selectorBigUniversitiesBuilder.add(e.getId());
                        break;
                }
            }
            statusCount.tick();
        });
        statusCount.done("found %d households, " + "" +
                        "%d workplaces, " +
                        "%d kindergartens, " +
                        "%d primary schools, " +
                        "%d high schools, " +
                        "%d agents, " +
                        "%d big unis, " +
                        "%d small unis",
                countGd.get(),
                countZaklady.get(),
                countKindergartens.get(),
                countPrimarySchools.get(),
                countHighSchools.get(),
                countAgenci.get(),
                countBigUniversities.get(),
                countSmallUniversities.get());

        var statusExport = Status.of("Going over households and exporting data", 1_000_000);
        idExporter.create(countAgenci.get());
        try (var datGd = debugTextFileService.createTextFile(new File(dir, "gd.dat").getPath());
             var datAgenci = debugTextFileService.createTextFile(new File(dir, "agenci.dat").getPath())) {

            var agentId = new AtomicInteger();
            datGd.printlnf("IloscGD %d", countGd.get());
            datAgenci.printlnf("IloscAgentow %d", countAgenci.get());

            engineIo.getEngine().execute(select(selectorGdBuilder).forEach(householdEntity -> {
                var household = householdEntity.get(Household.class);
                var cell = KilometerGridCell.fromLocation(householdEntity.get(Location.class));
                var members = household.getMembers();

                datGd.printf("%d", members.size());
                members.forEach(memberEntity -> {
                    var person = memberEntity.get(Person.class);
                    var pdyn1Id = agentId.getAndIncrement();
                    var pdyn2Id = memberEntity.getId();

                    idExporter.saveIdMapping(pdyn1Id, pdyn2Id);
                    datGd.printf(" %d", pdyn1Id);
                    datAgenci.printlnf("%d %d", person.getAge(), person.getSex().ordinal());
                    var attendee = memberEntity.get(Attendee.class);
                    if (attendee != null) {
                        if (attendee.getInstitution() != null) {
                            addToAttendees(pdyn1Id, attendee.getInstitution());
                        }
                        if (attendee.getSecondaryInstitution() != null) {
                            addToAttendees(pdyn1Id, attendee.getSecondaryInstitution());
                        }
                    }
                    var employee = memberEntity.get(Employee.class);
                    if (employee != null) {
                        addToAttendees(pdyn1Id, employee.getWork());
                    }
                });
                datGd.println();
                datGd.printlnf("%d %d", cell.getLegacyPdynRow(), cell.getLegacyPdynCol());

                statusExport.tick();
            }));
        }
        statusExport.done();

        var statusIds = Status.of("Saving agent IDs mapping");
        idExporter.saveToFile(Path.of(dir,"ids_mapping.orc").toString());
        statusIds.done();

        var cells = communeManager.getCommunes()
                .stream().collect(Collectors.toUnmodifiableMap(
                        c -> c.getTeryt(),
                        c -> new ArrayList<>(c.getCells())));

        var statusZaklady = Status.of("Exporting zaklady.dat", 100_000);
        try (var datZaklady = debugTextFileService.createTextFile(new File(dir, "zaklady.dat").getPath())) {
            datZaklady.printlnf("%d", countZaklady.get());
            engineIo.getEngine().execute(select(selectorZakladyBuilder).forEach(e -> {
                var unit = e.get(AdministrationUnitTag.class);
                var possibilities = cells.get(unit.getTeryt());
                var cell = possibilities.get(random.nextInt(possibilities.size()));
                var workplaceAttendees = attendees.getOrDefault(e.getId(), IntLists.EMPTY_LIST).toIntArray();
                datZaklady.printf("%d", workplaceAttendees.length);
                for (int id : workplaceAttendees) {
                    datZaklady.printf("\t%d", id);
                }
                datZaklady.println();
                datZaklady.printlnf("%d  %d  0", cell.getLegacyPdynRow(), cell.getLegacyPdynCol());
                statusZaklady.tick();
            }));
        }
        statusZaklady.done();

        var statusEdu = Status.of("Exporting szkoly.dat", 100_000);
        try (var datEdu = debugTextFileService.createTextFile(new File(dir, "szkoly.dat").getPath())) {
            if (removeEmptyEduInstitutions) {
                engineIo.getEngine().execute(select(selectorKindertardensBuilder).forEach(entity -> {
                    if (attendees.getOrDefault(entity.getId(), IntLists.EMPTY_LIST).isEmpty()) {
                        countKindergartens.decrementAndGet();
                    }
                }));
                engineIo.getEngine().execute(select(selectorPrimarySchoolsBuilder).forEach(entity -> {
                    if (attendees.getOrDefault(entity.getId(), IntLists.EMPTY_LIST).isEmpty()) {
                        countPrimarySchools.decrementAndGet();
                    }
                }));
            }
            datEdu.printlnf("LiczbaPrzedszkoli %d", countKindergartens.get());
            datEdu.printlnf("LiczbaPodstawowek %d", countPrimarySchools.get());
            datEdu.printlnf("LiczbaGimnazjow 0");

            // nurseries
            engineIo.getEngine().execute(select(selectorKindertardensBuilder).forEach(e -> {
                var cell = KilometerGridCell.fromLocation(e.get(Location.class));
                var nurseryAttendees = attendees.getOrDefault(e.getId(), IntLists.EMPTY_LIST).toIntArray();
                if (!removeEmptyEduInstitutions || nurseryAttendees.length > 0) {
                    datEdu.printf("%d", nurseryAttendees.length);
                    for (int id : nurseryAttendees) {
                        datEdu.printf("\t%d", id);
                    }
                    datEdu.printlnf("");
                    datEdu.printlnf("%d %d", cell.getLegacyPdynRow(), cell.getLegacyPdynCol());
                }
                statusEdu.tick();
            }));

            // schools
            engineIo.getEngine().execute(select(selectorPrimarySchoolsBuilder).forEach(e -> {
                var cell = KilometerGridCell.fromLocation(e.get(Location.class));
                var primaryAttendees = attendees.getOrDefault(e.getId(), IntLists.EMPTY_LIST).toIntArray();
                if (!removeEmptyEduInstitutions || primaryAttendees.length > 0) {
                    datEdu.printf("%d", primaryAttendees.length);
                    for (int id : primaryAttendees) {
                        datEdu.printf("\t%d", id);
                    }
                    datEdu.printlnf("");
                    datEdu.printlnf("%d %d", cell.getLegacyPdynRow(), cell.getLegacyPdynCol());
                }
                statusEdu.tick();
            }));
            statusEdu.done();
        }

        saveEduInstitutions(dir, selectorHighSchoolBuilder, countHighSchools,
                "licea.dat", "");
        saveEduInstitutions(dir, selectorBigUniversitiesBuilder, countBigUniversities,
                "djew.dat", " university");
        saveEduInstitutions(dir, selectorSmallUniversitiesBuilder, countSmallUniversities,
                "jew.dat", "");

    }

    private void addToAttendees(int id, Entity attendedEntity) {
        attendees
                .computeIfAbsent(attendedEntity.getId(), x -> new IntArrayList(500))
                .add(id);
    }

    private void saveEduInstitutions(String dir, Selector selectorEduInstitutionsBuilder,
                                     AtomicInteger eduInstitutionsCount,
                                     String fileName, String eduInstitutionsName) throws IOException {

        if (removeEmptyEduInstitutions) {
            engineIo.getEngine().execute(select(selectorEduInstitutionsBuilder).forEach(entity -> {
                if (attendees.getOrDefault(entity.getId(), IntLists.EMPTY_LIST).isEmpty()) {
                    eduInstitutionsCount.decrementAndGet();
                }
            }));
        }
        var statusEduInstitutions = Status.of("Exporting " + fileName, 100_000);
        try (var datEduInstitutions = debugTextFileService.createTextFile(new File(dir, fileName).getPath())) {
            datEduInstitutions.printlnf("%d", eduInstitutionsCount.get());
            engineIo.getEngine().execute(select(selectorEduInstitutionsBuilder).forEach(entity -> {
                KilometerGridCell location = KilometerGridCell.fromLocation(entity.get(Location.class));
                var universityAttendees = attendees.getOrDefault(entity.getId(), IntLists.EMPTY_LIST).toIntArray();
                if (!removeEmptyEduInstitutions || universityAttendees.length != 0) {
                    datEduInstitutions.printf("%d", universityAttendees.length);
                    for (int id : universityAttendees) {
                        datEduInstitutions.printf("\t%d", id);
                    }
                    datEduInstitutions.println();
                    datEduInstitutions.printlnf("%d %d%s", location.getLegacyPdynRow(), location.getLegacyPdynCol(), eduInstitutionsName);
                    statusEduInstitutions.tick();
                }
            }));
        }
        statusEduInstitutions.done();
    }

    private Stream<Entity> householdsAndWorkplacesAndEduInstitutions$() {
        return engineIo.getEngine()
                .streamDetached()
                .filter(e -> e.get(Household.class) != null ||
                        e.get(Workplace.class) != null ||
                        e.get(EducationalInstitution.class) != null);
    }
}
