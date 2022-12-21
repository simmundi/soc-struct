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
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.density.PopulationDensityLoader;
import pl.edu.icm.board.model.*;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.board.urizen.generic.EntityStreamManipulator;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.bin.BinPoolsByShape;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.util.EntityIterator;
import pl.edu.icm.trurl.ecs.util.StaticSelectors;
import pl.edu.icm.trurl.ecs.selector.Selector;
import pl.edu.icm.trurl.util.Status;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class AssignPatientsToHealthcareUrizen {
    private final Board board;
    private final Entities entities;
    private final EntityStreamManipulator entityStreamManipulator;
    private final RandomGenerator random;
    private final PopulationDensityLoader populationDensityLoader;
    private final StaticSelectors staticSelectors;

    @WithFactory
    public AssignPatientsToHealthcareUrizen(Board board,
                                            Entities entities,
                                            EntityStreamManipulator entityStreamManipulator,
                                            RandomProvider randomProvider,
                                            PopulationDensityLoader populationDensityLoader,
                                            StaticSelectors staticSelectors) {
        this.board = board;
        this.entities = entities;
        this.entityStreamManipulator = entityStreamManipulator;
        this.random = randomProvider.getRandomGenerator(AssignPatientsToHealthcareUrizen.class);
        this.populationDensityLoader = populationDensityLoader;
        this.staticSelectors = staticSelectors;
        board.require(
                Household.class,
                Named.class,
                Person.class,
                Location.class,
                AdministrationUnit.class,
                Workplace.class,
                EducationalInstitution.class,
                Attendee.class,
                Employee.class,
                Replicant.class,
                Complex.class,
                Healthcare.class,
                Patient.class);
    }

    public void assignToHealthcare() {
        Engine engine = board.getEngine();
        var statusIndexing = Status.of("Indexing households");
        Selector householdsSelector = staticSelectors.select(staticSelectors.config()
                .withMandatoryComponents(Household.class)
                .build());
        statusIndexing.done();
        populationDensityLoader.loadActualPopulationFromEngine();
        assign(householdsSelector, mapPossibilities(2));
        assign(householdsSelector, mapPossibilities(5));
        assign(householdsSelector, mapPossibilities(10));
        var leftGridCells = assign(householdsSelector, mapPossibilities(30));
        assign(householdsSelector, mapPossibilities(leftGridCells, 70));
    }

    private Set<KilometerGridCell> assign(Selector householdsSelector, BinPoolsByShape<KilometerGridCell, Entity> healthcareUnits) {
        Engine engine = board.getEngine();
        Set<KilometerGridCell> unassignedCells = new HashSet<>();
        var status = Status.of("Assigning patients to healthcare units", 500000);
        engine.execute(EntityIterator.select(householdsSelector).forEach(householdEntity -> {
            Entity unit = null;
            KilometerGridCell cell = null;
            try {
                status.tick();
                cell = KilometerGridCell.fromLocation(householdEntity.get(Location.class));
                List<Entity> members = householdEntity.get(Household.class).getMembers();
                for (Entity member : members) {
                    if (member.get(Patient.class) != null) {
                        cell = null;
                        break;
                    }
                    unit = findUnitIfNotAlreadyFound(healthcareUnits, cell, unit);
                    entities.createPatient(member, unit);
                }
            } catch (NullPointerException re) {
                status.problem("Couldn't assign attendee(s) to healthcare unit");
            } catch (RuntimeException re) {
                status.problem("Unexpected error: " + re.getMessage());
            }
            if (unit == null && cell != null) {
                unassignedCells.add(cell);
            }
        }));
        status.done();
        return unassignedCells;
    }

    private Entity findUnitIfNotAlreadyFound(BinPoolsByShape<KilometerGridCell, Entity> gridCellEntityBinsByShape, KilometerGridCell cell, Entity found) {
        if (found != null) {
            return found;
        }
        return gridCellEntityBinsByShape
                .getGroupedBins()
                .get(cell)
                .sample(random.nextDouble())
                .pick();
    }

    private BinPoolsByShape<KilometerGridCell, Entity> mapPossibilities(int radius) {
        int skip = 2000 * radius * radius;
        var status = Status.of("Mapping slots in healthcare: radius = " + radius + " km", skip);
        Engine engine = board.getEngine();
        var result = entityStreamManipulator.groupIntoShapes(
                engine.streamDetached()
                        .filter(entity -> entity.get(Healthcare.class) != null),
                entity -> 10000,
                entity -> KilometerGridCell.fromLocation(entity.get(Location.class))
                        .neighboringCircle(radius).filter(populationDensityLoader::isPopulated)
                        .flatMap(cell -> {
                            status.tick();

                            return Stream.of(cell);
                        })
        );
        status.done();
        return result;
    }

    private BinPoolsByShape<KilometerGridCell, Entity> mapPossibilities(Set<KilometerGridCell> cells, int radius) {
        int skip = 2000000;
        var status = Status.of("Final mapping of slots in healthcare", skip);
        Engine engine = board.getEngine();
        var result = entityStreamManipulator.groupIntoShapes(
                engine.streamDetached()
                        .filter(entity -> entity.get(Healthcare.class) != null),
                entity -> 10000,
                entity -> KilometerGridCell.fromLocation(entity.get(Location.class))
                        .neighboringCircle(radius)
                        .filter(cells::contains)
                        .flatMap(cell -> {
                            status.tick();

                            return Stream.of(cell);
                        })
        );
        status.done();
        return result;
    }
}
