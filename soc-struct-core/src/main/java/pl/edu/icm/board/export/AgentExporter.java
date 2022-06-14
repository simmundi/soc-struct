package pl.edu.icm.board.export;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.util.EntityIterator;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.util.Status;
import pl.edu.icm.trurl.visnow.VnPointsExporter;

import java.io.IOException;

public class AgentExporter {

    private final Engine engine;
    private final Selectors selectors;

    @WithFactory
    public AgentExporter(Board board, Selectors selectors) {
        this.engine = board.getEngine();
        this.selectors = selectors;
    }

    public void export() throws IOException {
        var exporter = VnPointsExporter.create(
                ExportedAgent.class,
                "output/agenci");

        ExportedAgent exportedAgent = new ExportedAgent();
        var statusBar = Status.of("Outputing agents", 500000);
        engine.execute(EntityIterator.select(selectors.allWithComponents(Household.class, Location.class))
                .dontPersist()
                .forEach(Household.class, Location.class, (entity, household, location) -> {
                    for (Entity member : household.getMembers()) {
                        var person = member.get(Person.class);

                        exportedAgent.setAge((byte) Math.min(person.getAge(), 127));
                        exportedAgent.setX(location.getE() / 1000f);
                        exportedAgent.setY(location.getN() / 1000f);
                        exportedAgent.setSex(person.getSex() == Person.Sex.M);

                        exporter.append(exportedAgent);

                        statusBar.tick();
                    }
                }));
        exporter.close();
        statusBar.done();
    }
}
