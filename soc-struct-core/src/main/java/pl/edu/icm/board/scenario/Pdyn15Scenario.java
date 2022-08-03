package pl.edu.icm.board.scenario;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.BoardFactory;
import pl.edu.icm.em.common.EmConfig;
import pl.edu.icm.board.urizen.household.HouseholdUrizen;
import pl.edu.icm.board.urizen.household.HouseholdsFromGridToAddressPointsUrizen;
import pl.edu.icm.board.urizen.household.HouseholdsToGridUrizen;
import pl.edu.icm.board.urizen.household.cloner.HouseholdClonerUrizen;
import pl.edu.icm.board.urizen.person.PersonNameUrizen;
import pl.edu.icm.board.urizen.place.AssignAttendeesToInstitutionsUrizen;
import pl.edu.icm.board.urizen.place.EducationalInstitutionEntitiesUrizen;
import pl.edu.icm.board.urizen.place.WorkplacesUrizen;
import pl.edu.icm.board.urizen.replicants.ReplicantsUrizen;
import pl.edu.icm.board.urizen.university.UniversityEntitiesUrizen;
import pl.edu.icm.board.util.CacheManager;

import java.io.IOException;

public class Pdyn15Scenario {
    private final PersonNameUrizen personNameUrizen;
    private final HouseholdUrizen householdUrizen;
    private final HouseholdClonerUrizen householdClonerUrizen;
    private final HouseholdsToGridUrizen householdsToGridUrizen;
    private final EducationalInstitutionEntitiesUrizen educationalInstitutionEntitiesUrizen;
    private final AssignAttendeesToInstitutionsUrizen assignAttendeesToInstitutionsUrizen;
    private final HouseholdsFromGridToAddressPointsUrizen householdsFromGridToAddressPointsUrizen;
    private final WorkplacesUrizen workplacesUrizen;
    private final UniversityEntitiesUrizen universityEntitiesUrizen;
    private final ReplicantsUrizen replicantsUrizen;

    @WithFactory
    public Pdyn15Scenario(
            CacheManager cacheManager,
            PersonNameUrizen personNameUrizen,
            HouseholdUrizen householdUrizen,
            HouseholdClonerUrizen householdClonerUrizen,
            HouseholdsToGridUrizen householdsToGridUrizen,
            EducationalInstitutionEntitiesUrizen educationalInstitutionEntitiesUrizen,
            AssignAttendeesToInstitutionsUrizen assignAttendeesToInstitutionsUrizen,
            HouseholdsFromGridToAddressPointsUrizen householdsFromGridToAddressPointsUrizen,
            WorkplacesUrizen workplacesUrizen,
            UniversityEntitiesUrizen universityEntitiesUrizen,
            ReplicantsUrizen replicantsUrizen) {
        this.personNameUrizen = personNameUrizen;
        this.householdUrizen = householdUrizen;
        this.householdClonerUrizen = householdClonerUrizen;
        this.householdsToGridUrizen = householdsToGridUrizen;
        cacheManager.freeAllCaches();

        this.educationalInstitutionEntitiesUrizen = educationalInstitutionEntitiesUrizen;
        this.assignAttendeesToInstitutionsUrizen = assignAttendeesToInstitutionsUrizen;
        this.householdsFromGridToAddressPointsUrizen = householdsFromGridToAddressPointsUrizen;
        this.workplacesUrizen = workplacesUrizen;
        this.universityEntitiesUrizen = universityEntitiesUrizen;
        this.replicantsUrizen = replicantsUrizen;
    }

    public void execute() {
        householdUrizen.createHouseholds();
        householdClonerUrizen.cloneHouseholds();
        personNameUrizen.giveNames();
        householdsToGridUrizen.allocateHouseholdsToGrid();
        educationalInstitutionEntitiesUrizen.buildEntities();
        universityEntitiesUrizen.buildEntities();
        assignAttendeesToInstitutionsUrizen.assignToInstitutions();
        householdsFromGridToAddressPointsUrizen.assignHouseholds();
        workplacesUrizen.createWorkplaces();
        workplacesUrizen.assignWorkersToWorkplaces();
        replicantsUrizen.createReplicants();
    }

    public static void main(String[] args) throws IOException {
        EmConfig.configurer(args)
                .loadConfigFile("output/households.properties")
                .loadConfigFile("output/workplaces.properties")
                .loadConfigFile("input/config/board/socstruct.properties")
                .loadConfigFile("input/config/board/replicants.properties")
                .use(Pdyn15ScenarioFactory.IT, scenario -> scenario.execute())
                .useWithIo(BoardFactory.IT, board -> board.save("output/scenario15.csv"));
    }
}
