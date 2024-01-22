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
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.education.AssigningMethod;
import pl.edu.icm.em.socstruct.component.NameTag;
import pl.edu.icm.em.socstruct.component.edu.EducationLevel;
import pl.edu.icm.board.education.EducationRadiusProvider;
import pl.edu.icm.em.socstruct.component.edu.EducationalInstitution;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.em.socstruct.component.geo.Location;
import pl.edu.icm.em.socstruct.component.Household;
import pl.edu.icm.em.socstruct.component.Person;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.board.urizen.generic.EntityStreamManipulator;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.bin.HistogramsByShape;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.util.EntityIterator;
import pl.edu.icm.trurl.ecs.util.StaticSelectors;
import pl.edu.icm.trurl.ecs.selector.Selector;
import pl.edu.icm.trurl.util.Status;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class AssignAttendeesToInstitutionsUrizen {
    private final EngineIo engineIo;
    private final EntityStreamManipulator entityStreamManipulator;
    private final Entities entities;
    private final RandomGenerator random;
    private final EducationRadiusProvider radius;
    private final StaticSelectors staticSelectors;

    @WithFactory
    public AssignAttendeesToInstitutionsUrizen(EngineIo engineIo,
                                               EntityStreamManipulator entityStreamManipulator,
                                               Entities entities,
                                               RandomProvider randomProvider,
                                               EducationRadiusProvider radius,
                                               StaticSelectors staticSelectors) {
        this.engineIo = engineIo;
        this.entityStreamManipulator = entityStreamManipulator;
        this.entities = entities;
        this.staticSelectors = staticSelectors;
        engineIo.require(EducationalInstitution.class, Location.class, NameTag.class, Household.class, Person.class);
        this.random = randomProvider.getRandomGenerator(AssignAttendeesToInstitutionsUrizen.class);
        this.radius = radius;
    }

    public void assignToInstitutions() {
        Engine engine = engineIo.getEngine();
        var statusIndexing = Status.of("Indexing households");
        Selector householdsSelector = staticSelectors
                .select(staticSelectors.config().withMandatoryComponents(Household.class).withInitialSize(14_000_000).build());
        statusIndexing.done();
        assign(householdsSelector,
                mapPossibilities(radius.getRadius(EducationLevel.K), e -> e.getLevel() == EducationLevel.K),
                p -> p.getAge() >= 2 && p.getAge() < 7,
                AssigningMethod.SELECT_PER_HOUSEHOLD,
                EducationLevel.K);
        assign(householdsSelector,
                mapPossibilities(radius.getRadius(EducationLevel.P), e -> e.getLevel() == EducationLevel.P || e.getLevel() == EducationLevel.PH),
                p -> p.getAge() >= 7 && p.getAge() < 15,
                AssigningMethod.SELECT_PER_HOUSEHOLD,
                EducationLevel.P);
        assign(householdsSelector,
                mapPossibilities(radius.getRadius(EducationLevel.H), e -> e.getLevel() == EducationLevel.H || e.getLevel() == EducationLevel.PH),
                p -> p.getAge() >= 15 && p.getAge() < 20,
                AssigningMethod.SELECT_PER_HOUSEHOLD,
                EducationLevel.H);
        assign(householdsSelector,
                mapPossibilities(radius.getRadius(EducationLevel.U), e -> e.getLevel() == EducationLevel.U),
                p -> p.getAge() >= 20 && p.getAge() < 26,
                AssigningMethod.SELECT_PER_ATTENDEE,
                EducationLevel.U);
        assign(householdsSelector,
                mapPossibilities(radius.getRadius(EducationLevel.BU), e -> e.getLevel() == EducationLevel.BU),
                p -> p.getAge() >= 20 && p.getAge() < 26,
                AssigningMethod.SELECT_PER_ATTENDEE,
                EducationLevel.BU);
    }

    private void assign(Selector householdsSelector,
                        HistogramsByShape<EducationInstitutionShape, Entity> gridCellEntityBinsByShape,
                        Predicate<Person> predicate,
                        AssigningMethod assigningMethod,
                        EducationLevel level) {
        Engine engine = engineIo.getEngine();
        var status = Status.of("Assigning attendees for level: " + level.name(), 500000);
        engine.execute(EntityIterator.select(householdsSelector).forEach(householdEntity -> {
            try {
                status.tick();
                List<Entity> members = householdEntity.get(Household.class).getMembers();
                Entity institution = null;
                KilometerGridCell kilometerGridCell = KilometerGridCell.fromLocation(householdEntity.get(Location.class));
                for (Entity member : members) {
                    if (predicate.test(member.get(Person.class))) {
                        institution = findInstitutionIfNotAlreadyFound(gridCellEntityBinsByShape,
                                institution,
                                kilometerGridCell,
                                level,
                                assigningMethod);
                        if (level == EducationLevel.BU) {
                            entities.attendsAsSecondary(member, institution);
                        } else entities.attends(member, institution);
                    }
                }
            } catch (NullPointerException re) {
                status.problem("Couldn't assign attendee(s) to educational institution");
            } catch (RuntimeException re) {
                status.problem("Unexpected error: " + re.getMessage());
            }
        }));
        status.done();
    }

    private Entity findInstitutionIfNotAlreadyFound(HistogramsByShape<EducationInstitutionShape, Entity> gridCellEntityBinsByShape, Entity found, KilometerGridCell cell, EducationLevel level, AssigningMethod method) {
        if (found != null && method == AssigningMethod.SELECT_PER_HOUSEHOLD) {
            return found;
        }
        return gridCellEntityBinsByShape
                .getGroupedBins()
                .get(EducationInstitutionShape.of(cell, level))
                .sample(random.nextDouble())
                .pick();
    }

    private HistogramsByShape<EducationInstitutionShape, Entity> mapPossibilities(int radius, Predicate<EducationalInstitution> predicate) {
        var status = Status.of("Mapping slots in educational institutions", 1000000);
        Engine engine = engineIo.getEngine();
        var result = entityStreamManipulator.groupIntoShapes(
                engine.streamDetached()
                        .filter(entity -> entity.get(EducationalInstitution.class) != null && predicate.test(entity.get(EducationalInstitution.class))),
                entity -> entity.get(EducationalInstitution.class).getEstPupilCount(),
                entity -> KilometerGridCell.fromLocation(entity.get(Location.class))
                        .neighboringCircle(radius)
                        .flatMap(cell -> {
                            status.tick();
                            EducationLevel level = entity.get(EducationalInstitution.class).getLevel();
                            if (level == EducationLevel.PH) {
                                return Stream.of(EducationInstitutionShape.of(cell, EducationLevel.P), EducationInstitutionShape.of(cell, EducationLevel.H));
                            } else
                                return Stream.of(EducationInstitutionShape.of(cell, level));

                        })
        );
        status.done();
        return result;
    }

    private static class EducationInstitutionShape {
        public final KilometerGridCell kilometerGridCell;
        public final EducationLevel educationLevel;

        private EducationInstitutionShape(KilometerGridCell kilometerGridCell, EducationLevel educationLevel) {
            this.kilometerGridCell = kilometerGridCell;
            this.educationLevel = educationLevel;
        }

        public static EducationInstitutionShape of(KilometerGridCell cell, EducationLevel educationLevel) {
            return new EducationInstitutionShape(cell, educationLevel);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EducationInstitutionShape that = (EducationInstitutionShape) o;
            return kilometerGridCell.equals(that.kilometerGridCell) &&
                    educationLevel == that.educationLevel;
        }

        @Override
        public int hashCode() {
            return Objects.hash(kilometerGridCell, educationLevel);
        }
    }
}
