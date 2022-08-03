package pl.edu.icm.board.urizen.university;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.board.geography.KilometerGridCell;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
class UniversityLoaderTest {

    @Test
    @DisplayName("Should correctly parse university definition file")
    void load() {
        //given
        var textFileLines = List.of("2",
                "3 2 1 0",
                "100 80 Uni1",
                "1 4",
                "10 10 Uni2");
        //execute
        UniversityLoader loader = new UniversityLoader("big", "small", null);
        var universityList = loader.load(textFileLines);
        //assert
        assertThat(universityList)
                .contains(
                        new University(
                                KilometerGridCell.fromLegacyPdynCoordinates(80, 100).toLocation(), 3
                        ),
                        new University(
                                KilometerGridCell.fromLegacyPdynCoordinates(10, 10).toLocation(), 1)
                );

    }

    @Test
    @DisplayName("Should correctly parse university definition file")
    void parseUniversityDefinition() {
        //given
        String universityStudents = "3 12 4 0";
        String locationAndName = "5 3 GloriousUniversity";
        String locationNoName = "1 3";

        //execute
        UniversityLoader loader = new UniversityLoader("big", "small", null);
        University bigUniversity = loader.parseUniversityDefinition(universityStudents, locationAndName);
        University smallUniversity = loader.parseUniversityDefinition(universityStudents, locationNoName);

        //assert
        assertThat(bigUniversity.studentCount).isEqualTo(3);
        assertThat(KilometerGridCell.fromLocation(bigUniversity.getLocation()).getLegacyPdynRow()).isEqualTo(5);
        assertThat(KilometerGridCell.fromLocation(bigUniversity.getLocation()).getLegacyPdynCol()).isEqualTo(3);

        assertThat(smallUniversity.studentCount).isEqualTo(3);
        assertThat(KilometerGridCell.fromLocation(smallUniversity.getLocation()).getLegacyPdynRow()).isEqualTo(1);
        assertThat(KilometerGridCell.fromLocation(smallUniversity.getLocation()).getLegacyPdynCol()).isEqualTo(3);
    }
}
