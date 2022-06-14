package pl.edu.icm.board.agesex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.household.model.AgeRange;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AgeSexFromDistributionPickerTest {
    @Test
    @DisplayName("Should give values in the correct range")
    void getRandomAge() {
        //given
        var ageSexFromDistributionPicker = new AgeSexFromDistributionPicker("src/test/resources/testAgeDistr.csv");
        List<Integer> results1 = new ArrayList<>();
        List<Integer> results2 = new ArrayList<>();
        //execute
        for (int i = 0; i < 5; i++){
            results1.add(ageSexFromDistributionPicker.getEmpiricalDistributedRandomAge(Person.Sex.M, AgeRange.AGE_10_14, 0));
            results2.add(ageSexFromDistributionPicker.getEmpiricalDistributedRandomAge(Person.Sex.K, AgeRange.AGE_15_19, 0));
        }
        //assert
        assertThat(results1).containsExactlyElementsOf(List.of(10, 10, 11, 11, 11));
        assertThat(results2).containsExactlyElementsOf(List.of(16, 17, 17, 19, 19));
    }
}
