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

package pl.edu.icm.em.socstruct.component.edu;

import pl.edu.icm.trurl.ecs.dao.annotation.CategoryManagedBy;
import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

@WithDao
public class EducationalInstitution {
    @CategoryManagedBy(EducationLevelManager.class)
    private EducationLevel level;
    private int estPupilCount;
    private short estTeacherCount;

    public EducationalInstitution() {
    }

    public EducationalInstitution(EducationLevel level, int estPupilCount, int estTeacherCount) {
        this.level = level;
        this.estPupilCount = estPupilCount;
        this.estTeacherCount = (short) estTeacherCount;
    }

    public EducationLevel getLevel() {
        return level;
    }

    public void setLevel(EducationLevel level) {
        this.level = level;
    }

    public int getEstPupilCount() {
        return estPupilCount;
    }

    public void setEstPupilCount(int estPupilCount) {
        this.estPupilCount = estPupilCount;
    }

    public short getEstTeacherCount() {
        return estTeacherCount;
    }

    public void setEstTeacherCount(short estTeacherCount) {
        this.estTeacherCount = estTeacherCount;
    }
}
