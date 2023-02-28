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

package pl.edu.icm.board.urizen.replicants;

import net.snowyhollows.bento.annotation.WithFactory;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.agesex.AgeSexFromDistributionPicker;
import pl.edu.icm.board.model.Complex;
import pl.edu.icm.board.model.EducationalInstitution;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.geography.density.PopulationDensityLoader;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Replicant;
import pl.edu.icm.board.model.Attendee;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.board.urizen.generic.EntityStreamManipulator;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.bin.BinPoolsByShape;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.util.Status;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates <i>dormStudentCount</i> students, grouped in rooms of <i>dormRoomSize</i> people
 * stacked on top of each other in sets of <i>maxRoomsInDorm</i>.
 * <p>
 * The places for dorms are picked in distance of <i>dormToUniversityDistance</i> from
 * a university, but once a spot is selected, all students from the neghbouring
 * universities are allowed to live there.
 */
public class DormUrizen {
    private final EngineIo engineIo;
    private final ReplicantPrototypes prototypes;
    private final EntityStreamManipulator entityStreamManipulator;
    private final PopulationDensityLoader populationDensityLoader;
    private final RandomGenerator random;
    private final ReplicantsPopulation population;
    private final int dormReplicantsCount;
    private final int dormRoomSize;
    private final int dormMaxRooms;
    private final int dormToUniversityMaxDistance;
    private final AgeSexFromDistributionPicker ageSexFromDistributionPicker;
    private Entities entities;

    @WithFactory
    public DormUrizen(EngineIo engineIo,
                      ReplicantPrototypes replicantPrototypes,
                      Entities entities, EntityStreamManipulator entityStreamManipulator,
                      PopulationDensityLoader populationDensityLoader,
                      AgeSexFromDistributionPicker ageSexFromDistributionPicker,
                      RandomProvider randomProvider,
                      ReplicantsPopulation population,
                      int dormReplicantsCount,
                      int dormRoomSize,
                      int dormMaxRooms,
                      int dormToUniversityMaxDistance) {
        this.engineIo = engineIo;
        this.prototypes = replicantPrototypes;
        this.entities = entities;
        this.entityStreamManipulator = entityStreamManipulator;
        this.populationDensityLoader = populationDensityLoader;
        this.ageSexFromDistributionPicker = ageSexFromDistributionPicker;
        this.population = population;
        this.dormReplicantsCount = dormReplicantsCount;
        this.dormRoomSize = dormRoomSize;
        this.dormMaxRooms = dormMaxRooms;
        this.dormToUniversityMaxDistance = dormToUniversityMaxDistance;
        this.random = randomProvider.getRandomGenerator(DormUrizen.class);
        this.engineIo.require(Person.class, Household.class, Replicant.class, Attendee.class);
    }

    public void fabricate() throws FileNotFoundException {
        this.populationDensityLoader.load();
        var status = Status.of("building university map", 1000);
        BinPoolsByShape<KilometerGridCell, Entity> slotsInDorms = entityStreamManipulator.groupIntoShapes(
                engineIo.getEngine().streamDetached().filter(this::isEntityAUniversity),
                this::studentCount,
                entityStreamManipulator.cellsInRadius$(dormToUniversityMaxDistance)
        );
        status.done();

        int count = dormReplicantsCount;
        while (count > 0) {
            int size = Math.min((random.nextInt(dormMaxRooms) + 1) * dormRoomSize, count);
            generateDorm(slotsInDorms, size);
            count -= size;
        }
    }

    private void generateDorm(BinPoolsByShape<KilometerGridCell, Entity> slotsInDorms, int size) {
        Entity selectedUniversity = slotsInDorms.getAllBins().sample(random.nextDouble()).pick();

        // cells within correct radius and inhabited
        List<KilometerGridCell> acceptableCells =
                KilometerGridCell.fromLocation(selectedUniversity.get(Location.class))
                        .neighboringCircle(dormToUniversityMaxDistance)
                        .filter(populationDensityLoader::isPopulated)
                        .collect(Collectors.toList());

        if (acceptableCells.isEmpty()) {
            throw new IllegalStateException("University " + selectedUniversity.getId() + " has no acceptable neighbourhood");
        }
        engineIo.getEngine().execute(sessionFactory -> {
            Session session = sessionFactory.create();
            Entity entity = entities.createEmptyComplex(session, size);
            Complex complex = entity.get(Complex.class);
            complex.setType(Complex.Type.DORM);
            var selectedCell = acceptableCells.get(random.nextInt(acceptableCells.size()));
            entity.add(selectedCell.toLocation());
            List<Entity> complexHouseholds = complex.getHouseholds();

            int count = size;
            while (count > 0) {
                int roomSize = Math.min(dormRoomSize, count);
                createRoom(complexHouseholds, selectedCell, slotsInDorms, roomSize, session);
                count -= roomSize;
            }
        });

    }

    private void createRoom(List<Entity> complexHouseholds, KilometerGridCell selectedCell,
                            BinPoolsByShape<KilometerGridCell, Entity> slotsInDorms,
                            int roomSize, Session session) {
        List<Entity> students = prototypes.dormRoom(session, complexHouseholds, selectedCell).get(Household.class).getMembers();
        for (int i = 0; i < roomSize; i++) {
            students.add(createStudent(selectedCell, slotsInDorms, session));
        }
    }

    private Entity createStudent(
            KilometerGridCell selectedCell,
            BinPoolsByShape<KilometerGridCell, Entity> slotsInDorms,
            Session session) {
        var sexPicked = population.getPopulation().getPeopleBySex().sample(random.nextDouble()).pick();
        var ageRangePicked = population.getPopulation().getPeople20to24().sample(random.nextDouble()).pick();
        var student = prototypes.dormResident(session, sexPicked,
                ageSexFromDistributionPicker.getEmpiricalDistributedRandomAge(sexPicked, ageRangePicked, random.nextDouble())
        );
        Entity university = slotsInDorms.getGroupedBins().get(selectedCell).sample(random.nextDouble()).pick();
        student.add(new Attendee()).setInstitution(university);
        return student;
    }

    private boolean isEntityAUniversity(Entity entity) {
        EducationalInstitution educationalInstitution = entity.get(EducationalInstitution.class);
        return educationalInstitution != null && educationalInstitution.getLevel().isUniversity();
    }

    private int studentCount(Entity entity) {
        EducationalInstitution educationalInstitution = entity.get(EducationalInstitution.class);
        return educationalInstitution != null ? educationalInstitution.getPupilCount() : 0;
    }
}
