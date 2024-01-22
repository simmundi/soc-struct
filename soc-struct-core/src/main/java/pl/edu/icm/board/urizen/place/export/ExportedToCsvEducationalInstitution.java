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

package pl.edu.icm.board.urizen.place.export;

import pl.edu.icm.em.socstruct.component.geo.Location;
import pl.edu.icm.board.urizen.place.EducationalInstitutionFromCsv;
import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;
import pl.edu.icm.trurl.io.visnow.VnCoords;

@WithDao
public class ExportedToCsvEducationalInstitution implements VnCoords {
    float x;
    float y;
    short type;
    short pupils;

    public short getTeachers() {
        return teachers;
    }

    public void setTeachers(short teachers) {
        this.teachers = teachers;
    }

    short teachers;

    public ExportedToCsvEducationalInstitution() {
    }

    public ExportedToCsvEducationalInstitution(Location location, EducationalInstitutionFromCsv institution) {
        this.x = location.getE() / 1000f;
        this.y = location.getN() / 1000f;
        this.pupils = (short) institution.getPupils();
        this.teachers = (short) institution.getTeachers();
        if (institution.getLevel() == null) {
            type = 0;
        } else {
            switch (institution.getLevel()) {
                case PRESCHOOL:
                    type = 1; break;
                case PRIMARY:
                    type = 2; break;
                case PRIMARY_AND_HIGH:
                    type = 3; break;
                case HIGH:
                    type = 4; break;
                case ADULTS:
                    type = 5; break;
                default:
                    type = 0;
            }
        }
    }

    public short getPupils() {
        return pupils;
    }

    public void setPupils(short pupils) {
        this.pupils = pupils;
    }

    @Override
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    @Override
    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }
}
