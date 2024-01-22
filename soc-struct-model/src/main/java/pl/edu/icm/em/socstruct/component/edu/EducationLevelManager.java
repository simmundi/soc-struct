package pl.edu.icm.em.socstruct.component.edu;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.category.CategoryManager;

import java.util.List;

public class EducationLevelManager extends CategoryManager<EducationLevel> {

    @WithFactory
    public EducationLevelManager(Bento bento) {
        super(bento, "pl.edu.icm.em.socstruct.edu.level", EducationLevelFactory.IT);
    }

    @Override
    protected List<EducationLevel> getBuiltIns() {
        return List.of(EducationLevel.K, EducationLevel.P, EducationLevel.H, EducationLevel.PH, EducationLevel.BU, EducationLevel.U);
    }

    @Override
    public EducationLevel[] emptyArray() {
        return new EducationLevel[0];
    }
}
