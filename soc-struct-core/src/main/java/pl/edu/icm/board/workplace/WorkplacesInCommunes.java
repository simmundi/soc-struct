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

package pl.edu.icm.board.workplace;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.commune.Commune;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.util.Status;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WorkplacesInCommunes {

    private final WorkDir workDir;
    private final Board board;
    private final CommuneManager communeManager;
    private final ProfessionalActivityAssessor professionalActivityAssessor;
    private double employmentRate;
    private final String przeplywyLudnosciFilename;
    private final int totalSlotsInWorkContexts;

    private final Map<Commune, EmploymentInCommune> employment = new HashMap<>();

    @WithFactory
    public WorkplacesInCommunes(WorkDir workDir, Board board, CommuneManager communeManager, ProfessionalActivityAssessor professionalActivityAssessor, String przeplywyLudnosciFilename, int totalSlotsInWorkContexts) {
        this.workDir = workDir;
        this.board = board;
        this.communeManager = communeManager;
        this.professionalActivityAssessor = professionalActivityAssessor;
        this.przeplywyLudnosciFilename = przeplywyLudnosciFilename;
        this.totalSlotsInWorkContexts = totalSlotsInWorkContexts;
        board.require(Household.class, Person.class, Location.class);
    }

    public EmploymentInCommune getEmploymentDataFor(Commune commune) {
        return employment.get(commune);
    }


    public void calculateWorkplaces() throws IOException {
        if (!employment.isEmpty()) {
            return;
        }
        communeManager.getCommunes().forEach(commune -> {
            employment.put(commune, new EmploymentInCommune());
        });

        var status = Status.of("Finding potential employees", 1_000_000);

        board.getEngine().streamDetached()
                .filter(e -> e.get(Household.class) != null)
                .forEach(e -> {
                    status.tick();
                    var members = e.get(Household.class).getMembers();
                    var location = e.get(Location.class);
                    var commune = communeManager.communeAt(KilometerGridCell.fromLocation(location));
                    var inCommune = employment.get(commune);

                    for (Entity member : members) {
                        if (professionalActivityAssessor.assess(member.get(Person.class))) {
                            inCommune.potentialEmployees++;
                        }
                    }
                });
        status.done();

        var totalPotentialEmployees = employment.values().stream()
                .mapToInt(EmploymentInCommune::getPotentialEmployees).sum();

        employmentRate = (double)totalSlotsInWorkContexts / (double)totalPotentialEmployees;

        System.out.println(String.format("Found %d potential employees, %d are actually working; employment rate is %f", totalPotentialEmployees, totalSlotsInWorkContexts, employmentRate));

        // assume uniform distribution of non-traveling workers and create local
        // workplaces for every worker
        employment.forEach((commune, data) -> {
            int share = (int) Math.round(data.potentialEmployees * employmentRate);
            data.workplaces = share;
            data.localEmployees = share;
        });

        CsvParserSettings csvParserSettings = new CsvParserSettings();
        csvParserSettings.setLineSeparatorDetectionEnabled(true);
        csvParserSettings.setHeaderExtractionEnabled(true);

        Set<String> problematic = new HashSet<>();

        CsvParser csvParser = new CsvParser(csvParserSettings);
        csvParser.iterateRecords(workDir.openForReading(new File(przeplywyLudnosciFilename))).forEach(record -> {
            String terytOfResidence = record.getString(0);
            String terytOfEmplyment = record.getString(1);
            int travelCount = record.getInt(2);

            var communeOfResidence = communeManager.communeForTeryt(terytOfResidence);
            var communeOfEmployment = communeManager.communeForTeryt(terytOfEmplyment);

            if (communeOfResidence.isEmpty()) {
                problematic.add(terytOfResidence);
            }
            if (communeOfEmployment.isEmpty()) {
                problematic.add(terytOfEmplyment);
            }
            if (communeOfEmployment.isPresent() && communeOfResidence.isPresent()) {
                var inCommune = employment.get(communeOfResidence.get());
                inCommune.getTravelingEmployees().add(new Flow(communeOfEmployment.get(), travelCount));
                inCommune.localEmployees -= travelCount;
                inCommune.workplaces -= travelCount;

                employment.get(communeOfEmployment.get()).workplaces += travelCount;
            }
        });

    }

}
