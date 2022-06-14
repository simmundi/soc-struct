package pl.edu.icm.board.urizen.university;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.Location;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UniversityLoader {

    private final String bigUniversityFilename;
    private final String smallUniversityFileName;

    @WithFactory
    public UniversityLoader(String bigUniversityFilename, String smallUniversityFileName) {
        this.bigUniversityFilename = bigUniversityFilename;
        this.smallUniversityFileName = smallUniversityFileName;
    }

    public List<University> loadSmallUniversities () {
        return load(smallUniversityFileName);
    }

    public List<University> loadBigUniversities () {
        return load(bigUniversityFilename);
    }

    List<University> load (String fileName) {
        try {
            return load (Files.readAllLines(Paths.get(fileName)));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    List<University> load (List<String> fileLines) {
            return IntStream.range(0, fileLines.size() / 2)
                    .map(lineNum -> lineNum * 2 + 1)
                    .mapToObj(lineNum -> parseUniversityDefinition(fileLines.get(lineNum),
                            fileLines.get(lineNum + 1)))
                    .collect(Collectors.toList());
    }

    University parseUniversityDefinition (String studentsLine, String positionNameLine) {
        int numberOfStudents = Integer.parseInt(studentsLine.split("\\s")[0]);
        var split = positionNameLine.split("\\s");
        int row = Integer.parseInt(split[0]),
                col = Integer.parseInt(split[1]);
        KilometerGridCell cell = KilometerGridCell.fromLegacyPdynCoordinates(col, row);
        Location location = cell.toLocation();
        return new University(location, numberOfStudents);
    }
}
