package pl.edu.icm.board.urizen.place;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.model.EducationLevel;
import pl.edu.icm.board.model.EducationalInstitution;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.model.Named;
import pl.edu.icm.board.model.AdministrationUnit;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.util.Status;

import java.io.IOException;

public class EducationalInstitutionEntitiesUrizen {

    private final EducationInstitutionGeodecoder educationInstitutionGeodecoder;
    private final Board board;
    private final Entities entities;

    @WithFactory
    public EducationalInstitutionEntitiesUrizen(EducationInstitutionGeodecoder educationInstitutionGeodecoder, Board board, Entities entities) {
        this.educationInstitutionGeodecoder = educationInstitutionGeodecoder;
        this.board = board;
        this.entities = entities;

        this.board.require(EducationalInstitution.class, Named.class, Location.class, AdministrationUnit.class);
    }

    public void buildEntities() {
        var status = Status.of("Adding institutions");
        board.getEngine().execute(sessionFactory -> {
            Session session = sessionFactory.create();
            try {
                educationInstitutionGeodecoder.foreach(geodecoded -> {
                    EducationLevel level = EducationalInstitutionFromCsv.fromLevel(geodecoded.getPoi().getLevel());
                    if (level != null) {
                        entities.createEducationInstitution(
                                session,
                                geodecoded.getPoi().getName(),
                                geodecoded.getAddressLookupResult().getLocation(),
                                level,
                                geodecoded.getPoi().getPupils(),
                                geodecoded.getPoi().getTeachers(),
                                geodecoded.getPoi().getCommuneTeryt());
                    }
                    session.close();
                    status.tick();
                });
                status.done();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
