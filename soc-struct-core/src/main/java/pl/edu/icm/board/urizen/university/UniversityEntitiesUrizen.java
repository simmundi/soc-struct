package pl.edu.icm.board.urizen.university;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.model.EducationLevel;
import pl.edu.icm.board.model.EducationalInstitution;
import pl.edu.icm.board.model.Named;
import pl.edu.icm.board.model.AdministrationUnit;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.urizen.generic.Entities;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.util.Status;

public class UniversityEntitiesUrizen {
    private final UniversityLoader universityLoader;
    private final Board board;
    private final Entities entities;
    private final double universityRadius;

    @WithFactory
    public UniversityEntitiesUrizen(UniversityLoader universityLoader,
                                    Board board,
                                    Entities entities,
                                    int universityRadius) {

        this.universityLoader = universityLoader;
        this.board = board;
        this.entities = entities;
        this.universityRadius = universityRadius;

        board.require(
                EducationalInstitution.class,
                Location.class,
                Named.class,
                AdministrationUnit.class);
    }

    public void buildEntities() {
        var status = Status.of("Building universities");
        board.getEngine().execute(sessionFactory -> {
            Session session = sessionFactory.create();
            var bigUniversities = universityLoader.loadBigUniversities();
            var smallUniversities = universityLoader.loadSmallUniversities();
            for (University bigUniversity : bigUniversities) {
                entities.createEducationInstitution(session,
                        "",
                        bigUniversity.getLocation(),
                        EducationLevel.BU,
                        bigUniversity.getStudentCount(),
                        0,
                        "");
                status.tick();
            }

            for (University smallUniversity : smallUniversities) {
                entities.createEducationInstitution(session,
                        "",
                        smallUniversity.getLocation(),
                        EducationLevel.U,
                        smallUniversity.getStudentCount(),
                        0,
                        "");
                status.tick();
            }
            session.close();
            status.done();
        });
    }
}
