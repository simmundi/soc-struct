package pl.edu.icm.board.workplace;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.model.Person;

public class ProfessionalActivityAssessor {

    @WithFactory
    public ProfessionalActivityAssessor() {
    }

    public boolean assess(Person person) {
        return person.getAge() >= 19 && person.getAge() <= retirementAge(person.getSex());

    }

    private int retirementAge(Person.Sex sex) {
        switch (sex) {
            case K: return 60;
            default:
            case M: return 65;
        }
    }
}
