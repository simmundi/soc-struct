/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.board.urizen.university;

import net.snowyhollows.bento.annotation.WithFactory;
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
    private final String rootPath;

    @WithFactory
    public UniversityLoader(String bigUniversityFilename, String smallUniversityFileName, String rootPath) {
        this.bigUniversityFilename = bigUniversityFilename;
        this.smallUniversityFileName = smallUniversityFileName;
        this.rootPath = rootPath;
    }

    public List<University> loadSmallUniversities () {
        return load(smallUniversityFileName);
    }

    public List<University> loadBigUniversities () {
        return load(bigUniversityFilename);
    }

    List<University> load (String fileName) {
        try {
            return load (Files.readAllLines(Paths.get(rootPath, fileName)));
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
