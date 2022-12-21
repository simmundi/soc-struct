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

package pl.edu.icm.board.model;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class EducationalInstitution {
    private EducationLevel level;
    private int pupilCount;
    private short teacherCount;

    public EducationalInstitution() {
    }

    public EducationalInstitution(EducationLevel level, int pupilCount, int teacherCount) {
        this.level = level;
        this.pupilCount = pupilCount;
        this.teacherCount = (short)teacherCount;
    }

    public EducationLevel getLevel() {
        return level;
    }

    public void setLevel(EducationLevel level) {
        this.level = level;
    }

    public int getPupilCount() {
        return pupilCount;
    }

    public void setPupilCount(int pupilCount) {
        this.pupilCount = pupilCount;
    }

    public short getTeacherCount() {
        return teacherCount;
    }

    public void setTeacherCount(short teacherCount) {
        this.teacherCount = teacherCount;
    }
}
