package pl.edu.icm.board.export;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.BoardFactory;
import pl.edu.icm.em.common.EmConfig;
import pl.edu.icm.board.model.Attendee;
import pl.edu.icm.board.model.EducationalInstitution;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.util.Status;
import pl.edu.icm.trurl.visnow.VnPointsExporter;

import java.io.IOException;

import static pl.edu.icm.trurl.ecs.util.EntityIterator.select;

public class DistanceFromAttendeeExporter {

    private final Board board;
    private final String odleglosciPath;
    private final Selectors selectors;

    @WithFactory
    public DistanceFromAttendeeExporter(Board board,
                                        String odleglosciPath,
                                        Selectors selectors) {
        this.odleglosciPath = odleglosciPath;
        this.selectors = selectors;
        board.require(Household.class, Location.class, Attendee.class, EducationalInstitution.class, Person.class);
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
                        Attendee attendee = member.get(Attendee.class);
                        if (attendee != null) {
                            var educationalInstitution = attendee.getInstitution().get(EducationalInstitution.class);
                            if (educationalInstitution == null) {
                                continue;
                            }
                            var person = member.get(Person.class);
                            exported.setSex((short) person.getSex().ordinal());
                            exported.setX(location.getE() / 1000f);
                            exported.setY(location.getN() / 1000f);
                            var targetLocation = attendee.getInstitution().get(Location.class);
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
        var config = EmConfig.create(args);
        var board = config.get(BoardFactory.IT);
        var exporter = config.get(DistanceFromAttendeeExporterFactory.IT);

        board.loadOrc("output/5_people_households_edu_assigned.orc");
        exporter.export();
    }
}
