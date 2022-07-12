package pl.edu.icm.board.export;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.BoardFactory;
import pl.edu.icm.board.DefaultConfig;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.board.geography.commune.PopulationService;
import pl.edu.icm.board.model.*;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.ecs.util.Systems;
import pl.edu.icm.trurl.util.Status;
import pl.edu.icm.trurl.visnow.VnPointsExporter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static pl.edu.icm.trurl.ecs.util.EntityIterator.select;

public class DistanceFromHealthcareExporter {

    private final Board board;
    private final String healthcareDistanceExportFilename;
    private final Selectors selectors;
    private final CommuneManager communeManager;
    private final PopulationService populationService;

    @WithFactory
    public DistanceFromHealthcareExporter(Board board,
                                          String healthcareDistanceExportFilename,
                                          Selectors selectors,
                                          CommuneManager communeManager,
                                          PopulationService populationService) {
        this.healthcareDistanceExportFilename = healthcareDistanceExportFilename;
        this.selectors = selectors;
        board.require(Household.class, Location.class, Patient.class, Healthcare.class, Person.class);
        this.board = board;
        this.communeManager = communeManager;
        this.populationService = populationService;
    }

    public void export() throws IOException {
        populationService.load();
        Engine engine = board.getEngine();
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

    public void exportAgentsToCsv() throws IOException {
        var engine = board.getEngine();
        File csvOutputFile = new File("patients.csv");
        PrintWriter pw = new PrintWriter(csvOutputFile);
        pw.println("id,age,sex,teryt,healthcare_id,area_type\n");
        engine.execute(select(selectors.allWithComponents(Household.class, Location.class))
                .dontPersist()
                .forEach(Household.class, Location.class, (patientEntity, household, location) -> {
            for (Entity member : household.getMembers()) {
                var patient = member.get(Patient.class);
                if (patient != null) {
                    var person = member.get(Person.class);
                    var healthcareUnit = patient.getHealthcare().get(Healthcare.class);
                    pw.print(member.getId() + ",");
                    pw.print(person.getAge() + ",");
                    pw.print(person.getSex() + ",");
                    pw.print(communeManager.communeAt(KilometerGridCell.fromLocation(location)) + ",");
                    pw.print(healthcareUnit.getId() + ",");
                    pw.print(populationService.typeFromLocation(location) + "\n");
                }
            }
        }));
    }

    public static void main(String[] args) throws IOException {
        var config = DefaultConfig.createWith()
                .propertiesDir("input/config/healthcare")
                .propertiesDir("input/config/board")
                .getConfig();
        var board = config.get(BoardFactory.IT);
        var exporter = config.get(DistanceFromHealthcareExporterFactory.IT);

        board.load("output/added_healthcare.csv");
        exporter.exportAgentsToCsv();
    }
}
