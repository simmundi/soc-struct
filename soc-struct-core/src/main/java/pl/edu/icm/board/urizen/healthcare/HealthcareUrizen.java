package pl.edu.icm.board.urizen.healthcare;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.model.*;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.trurl.ecs.Session;

import java.io.IOException;

public class HealthcareUrizen {
    private final Board board;
    private final Entities entities;
    private final HealthcareGeodecoder healthcareGeodecoder;
    @WithFactory
    public HealthcareUrizen(
            Board board,
            Entities entities,
            HealthcareGeodecoder healthcareGeodecoder) {
        this.board = board;
        this.entities = entities;
        this.board.require(Location.class);
        this.healthcareGeodecoder = healthcareGeodecoder;
    }

    public void fabricate() {
        try {
            healthcareGeodecoder.foreach(healthcare -> {
                generateHealthcareUnit(healthcare.getPoi().getType(),
                        healthcare.getPoi().getDateOfClosure(),
                        healthcare.getAddressLookupResult().getLocation());
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateHealthcareUnit(HealthcareType type, String dateOfClosure, Location location) {
        if (type == HealthcareType.POZ && dateOfClosure.equals("NULL")) {
            board.getEngine().execute(sessionFactory -> {
                Session session = sessionFactory.create();
                entities.createHealthcare(session, type, location.getN(), location.getE());
                session.close();
            });
        }
    }
}
