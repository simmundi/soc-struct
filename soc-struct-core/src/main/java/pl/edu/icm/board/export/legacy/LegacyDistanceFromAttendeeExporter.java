package pl.edu.icm.board.export.legacy;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.export.DistanceFromAttendeeExporterFactory;
import pl.edu.icm.board.export.ExportedAttendee;
import pl.edu.icm.board.export.LegacyAttendee;
import pl.edu.icm.board.model.EducationalInstitution;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.BoardFactory;
import pl.edu.icm.board.DefaultConfig;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.util.Status;
import pl.edu.icm.trurl.visnow.VnPointsExporter;

import java.io.IOException;

import static pl.edu.icm.trurl.ecs.util.EntityIterator.select;

public class LegacyDistanceFromAttendeeExporter {

    private final Board board;
    private final String odleglosciPath;
    private final Selectors selectors;

    @WithFactory
    public LegacyDistanceFromAttendeeExporter(Board board,
                                              String odleglosciPath,
                                              Selectors selectors) {
        this.odleglosciPath = odleglosciPath;
        this.selectors = selectors;
        board.require(Household.class, Location.class, LegacyAttendee.class, EducationalInstitution.class, Person.class);
        this.board = board;
    }

    public void export() throws IOException {
        Engine engine = board.getEngine();
        var exporter = VnPointsExporter.create(
                ExportedAttendee.class,
                odleglosciPath);

        ExportedAttendee exported = new ExportedAttendee();
        var statusBar = Status.of("Outputing agents", 500000);

        engine.execute(
                select(selectors.allWithComponents(Household.class, Location.class))
                        .dontPersist()
                        .forEach(Household.class, Location.class, (householdEntity, household, location) -> {
                    for (Entity member : household.getMembers()) {
                        LegacyAttendee attendee = member.get(LegacyAttendee.class);
                        if (attendee != null) {
                            var educationalInstitution = attendee.getPlace().get(EducationalInstitution.class);
                            if (educationalInstitution == null) {
                                continue;
                            }
                            var person = member.get(Person.class);
                            exported.setSex((short) person.getSex().ordinal());
                            exported.setX(location.getE() / 1000f);
                            exported.setY(location.getN() / 1000f);
                            var targetLocation = attendee.getPlace().get(Location.class);
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
                }));
        exporter.close();
        statusBar.done();
    }

    public static void main(String[] args) throws IOException {
        var config = DefaultConfig.create();
        var board = config.get(BoardFactory.IT);
        var exporter = config.get(DistanceFromAttendeeExporterFactory.IT);

        board.load("input/vault/8_added_students.csv");
        exporter.export();
    }
}
