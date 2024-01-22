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
import net.snowyhollows.bento.config.WorkDir;
import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.commune.Commune;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.em.socstruct.component.geo.AdministrationUnitTag;
import pl.edu.icm.em.socstruct.component.work.Employee;
import pl.edu.icm.em.socstruct.component.Household;
import pl.edu.icm.em.socstruct.component.geo.Location;
import pl.edu.icm.em.socstruct.component.Person;
import pl.edu.icm.em.socstruct.component.work.Workplace;
import pl.edu.icm.board.urizen.generic.EntityStreamManipulator;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.board.workplace.ProfessionalActivityAssessor;
import pl.edu.icm.board.workplace.WorkplacesInCommunes;
import pl.edu.icm.em.common.DebugTextFile;
import pl.edu.icm.em.common.DebugTextFileService;
import pl.edu.icm.trurl.bin.Histogram;
import pl.edu.icm.trurl.bin.HistogramsByShape;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.util.Status;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static pl.edu.icm.trurl.ecs.util.EntityIterator.select;

public class WorkplacesUrizen {
    private final WorkDir workDir;
    private final WorkplacesInCommunes workplacesInCommunes;
    private final CommuneManager communeManager;
    private final EntityStreamManipulator entityStreamManipulator;
    private final EngineIo engineIo;
    private final ProfessionalActivityAssessor professionalActivityAssessor;
    private final RandomGenerator random;
    private final Indexes selectors;
    private final DebugTextFileService debugTextFileService;

    private final double[] workplaceCountsByEmployees;

    @WithFactory
    public WorkplacesUrizen(
            WorkplacesInCommunes workplacesInCommunes,
            EngineIo engineIo,
            int publicSectorZipfN,
            String publicSectorZipfS,
            int publicSectorTotal,
            int privateSectorZipfN,
            String privateSectorZipfS,
            int privateSectorTotal,
            WorkDir workDir, CommuneManager communeManager,
            EntityStreamManipulator entityStreamManipulator,
            ProfessionalActivityAssessor professionalActivityAssessor,
            RandomProvider randomProvider, Indexes selectors, DebugTextFileService debugTextFileService) {
        this.workplacesInCommunes = workplacesInCommunes;
        this.engineIo = engineIo;
        this.workDir = workDir;
        this.communeManager = communeManager;
        this.entityStreamManipulator = entityStreamManipulator;
        this.professionalActivityAssessor = professionalActivityAssessor;
        this.random = randomProvider.getRandomGenerator(WorkplacesUrizen.class);
        this.selectors = selectors;
        this.debugTextFileService = debugTextFileService;

        int maxEmployees = Math.max(publicSectorZipfN, privateSectorZipfN);
        workplaceCountsByEmployees = new double[maxEmployees];

        accumulateEmpoyeeSlots(publicSectorTotal, new ZipfDistribution(publicSectorZipfN, Double.parseDouble(publicSectorZipfS)));
        accumulateEmpoyeeSlots(privateSectorTotal, new ZipfDistribution(privateSectorZipfN, Double.parseDouble(privateSectorZipfS)));
        smearOneSlot();

        engineIo.require(Workplace.class, AdministrationUnitTag.class, Employee.class);
    }

    private void smearOneSlot() {
        // we want to remove all the workplaces with size of 1
        double toSmear = workplaceCountsByEmployees[1];
        workplaceCountsByEmployees[1] = 0;

        // we assume that the self-employed (aka b2b) only work in workplaces between 2-49
        double perBin = toSmear / 48.0;
        for (int i = 2; i <= 49; i++) {
            workplaceCountsByEmployees[i] += (perBin / i);
        }
    }

    private void accumulateEmpoyeeSlots(int total, ZipfDistribution distribution) {
        for (int i = 0; i < workplaceCountsByEmployees.length; i++) {
            workplaceCountsByEmployees[i] += distribution.probability(i) * total;
        }
    }

    public void createWorkplaces() {
        try {
            workplacesInCommunes.calculateWorkplaces();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        var engine = engineIo.getEngine();
        Histogram<Commune> employees = new Histogram<>();
        for (Commune commune : communeManager.getCommunes()) {
            employees.add(commune, workplacesInCommunes.getEmploymentDataFor(commune).getWorkplaces());
        }

        try (DebugTextFile pw = debugTextFileService.createTextFile("output/slots_in_communes.csv")) {
            pw.println("teryt,nazwa,slots");
            employees.streamBins().forEach(b -> {
                pw.println(String.format("%s,%s,%d", b.getLabel().getTeryt(), b.getLabel().getName(), b.getCount()));
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        var status = Status.of("Creating workplaces", 200000);
        engine.execute(sessionFactory -> {
            Session session = sessionFactory.create();
            double accumulator = 0.0;
            for (int i = workplaceCountsByEmployees.length - 1; i > 0; i--) {
                accumulator += workplaceCountsByEmployees[i];
                while (accumulator > 1.0) { // integerization
                    Entity workplaceEntity = session.createEntity();
                    workplaceEntity.add(new Workplace((short) i));
                    var choice = employees.sample(random.nextDouble()).pick(i);
                    workplaceEntity.add(new AdministrationUnitTag(choice.getTeryt()));
                    status.tick();
                    accumulator -= 1.0;
                }
            }
            session.close();
            status.done();
        });
    }

    public void assignWorkersToWorkplaces() {
        Status status = Status.of("grouping workplaces by communes");
        Map<Commune, Integer> slotsInExistingWorplaces = new HashMap<>();
        HistogramsByShape<Commune, Entity> workplacesByCommunes = entityStreamManipulator.groupIntoShapes(
                engineIo.getEngine().streamDetached()
                        .filter(e -> e.get(Workplace.class) != null),
                e -> e.get(Workplace.class).getEstEmployeeCount(),
                e -> Stream.of(
                        communeManager
                                .communeForTeryt(
                                        e.get(AdministrationUnitTag.class).getCode())
                                .get()
                )
        );
        workplacesByCommunes.getGroupedBins().entrySet().stream().forEach(entry -> {
            slotsInExistingWorplaces.put(entry.getKey(), entry.getValue().getTotalCount());
        });
        status.done();

        Map<Commune, Histogram<Commune>> flows = new HashMap<>();
        Map<Commune, Histogram<Classification>> classifications = new HashMap<>();

        try (var df = debugTextFileService.createTextFile("output/debug_flows.csv")) {
            df.println("commune,traveling,local,unemployed,workplaces");
            communeManager.getCommunes().forEach(commune -> {
                var employment = workplacesInCommunes.getEmploymentDataFor(commune);
                Histogram<Classification> classificationHistogram = new Histogram<>();
                classifications.put(commune, classificationHistogram);
                Histogram<Commune> flowsForOneCommune = new Histogram<>();
                flows.put(commune, flowsForOneCommune);

                employment.getTravelingEmployees().forEach(flow -> {
                    flowsForOneCommune.add(flow.getCommune(), flow.getNumberOfPeople());
                });

                var traveling = classificationHistogram.add(Classification.EMPLOYED_IN_ANOTHER_COMMUNE, flowsForOneCommune.getTotalCount());
                var local = classificationHistogram.add(Classification.EMPLOYED_IN_OWN_COMMUNE, employment.getLocalEmployees());
                var unemployed = classificationHistogram.add(Classification.UNEMPLOYED, employment.getPotentialEmployees() - classificationHistogram.getTotalCount());

                df.printf("%s,%d,%d,%d,%d\n", commune.getName(),
                        traveling.getCount(),
                        local.getCount(),
                        unemployed.getCount(),
                        slotsInExistingWorplaces.get(commune));
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        Set<Commune> failures = new HashSet<>();
        AtomicInteger successesLocal = new AtomicInteger();
        AtomicInteger successesTravel = new AtomicInteger();
        AtomicInteger unemployed = new AtomicInteger();
        AtomicInteger errors = new AtomicInteger();
        Status.of("Assigning " + workplacesByCommunes.getAllBins().getTotalCount() + " workplaces", 100000);
        engineIo.getEngine().execute(select(selectors.allWithComponents(Household.class, Location.class)).forEach(Household.class, Location.class, (e, household, location) -> {

            KilometerGridCell cell = KilometerGridCell.fromLocation(location);
            var commune = communeManager.communeAt(cell);
            var workplaces = workplacesByCommunes.getGroupedBins().get(commune);
            var flowsFromCommune = flows.get(commune);
            var classificationsForCommune = classifications.get(commune);

            if (workplaces == null) {
                failures.add(commune);
                return;
            }

            if (workplaces.getTotalCount() > 0 || flowsFromCommune.getTotalCount() > 0) {
                for (Entity member : household.getMembers()) {
                    Person person = member.get(Person.class);
                    boolean isAWorker = professionalActivityAssessor.assess(person);
                    if (isAWorker) {
                        Classification classification = classificationsForCommune.sample(random.nextDouble()).pick();
                        switch (classification) {
                            case EMPLOYED_IN_ANOTHER_COMMUNE:
                                var targetCommune = flowsFromCommune.sample(random.nextDouble()).pick();
                                var workplacesInAnotherCommune = workplacesByCommunes
                                        .getGroupedBins()
                                        .get(targetCommune);
                                if (workplacesInAnotherCommune == null) {
                                    failures.add(targetCommune);
                                    errors.incrementAndGet();
                                    break;
                                }
                                var targetWorkplaceInAnotherCommune = workplacesInAnotherCommune
                                        .sample(random.nextDouble())
                                        .pick();
                                member.add(new Employee()).setWork(targetWorkplaceInAnotherCommune);
                                status.tick();
                                successesTravel.incrementAndGet();
                                break;
                            case EMPLOYED_IN_OWN_COMMUNE:
                                var targetWorkplace = workplaces.sample(random.nextDouble()).pick();
                                member.add(new Employee()).setWork(targetWorkplace);
                                successesLocal.incrementAndGet();
                                status.tick();
                                break;
                            default:
                            case UNEMPLOYED:
                                unemployed.incrementAndGet();
                                break;
                        }
                    }
                }
            }
        }));
        status.done();
        System.out.println(String.format("Managed %d local, %d remote, %d unemployed and %d errors", successesLocal.get(), successesTravel.get(), unemployed.get(), errors.get()));
        try (DebugTextFile pw = debugTextFileService.createTextFile("output/unused_slots_in_communes.csv")) {
            pw.println("teryt,nazwa,unusedSlots");
            workplacesByCommunes.getGroupedBins().entrySet().forEach(b -> {
                pw.println(String.format("%s,%s,%d", b.getKey().getTeryt(), b.getKey().getName(), b.getValue().getTotalCount()));
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        System.out.println("Failures:");
        System.out.println(failures);
        System.out.println("==============");
    }

    private enum Classification {
        UNEMPLOYED,
        EMPLOYED_IN_OWN_COMMUNE,
        EMPLOYED_IN_ANOTHER_COMMUNE
    }
}
